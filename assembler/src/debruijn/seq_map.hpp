/*
 * seq_map.hpp
 *
 *  Created on: 22.04.2011
 *      Author: vyahhi
 */

#ifndef SEQ_MAP_HPP_
#define SEQ_MAP_HPP_

#include "read.hpp"
#include "sequence.hpp"
#include "seq.hpp"
#include "cuckoo.hpp"
//#include <tr1/unordered_map>

/*
 * act as DeBruijn graph and Index at the same time :)
 *
 * size_ here is as K+1 in other parts of code
 *
 * Map from Seq<size_> to (Value, size_t)
 * where Value is usually EdgeId and size_t is offset where this Seq is in EdgeId
 *
 */
namespace de_bruijn {
LOGGER("d.utils");

template<size_t size_, typename Value>
class SeqMap {
private:
	typedef Seq<size_> KPlusOneMer;
	typedef Seq<size_ - 1> KMer;
	//typedef std::tr1::unordered_map<KPlusOneMer, pair<Value, size_t> ,
	//	typename KPlusOneMer::hash, typename KPlusOneMer::equal_to> map_type; // size_t is offset
	typedef cuckoo<KPlusOneMer, pair<Value, size_t>, typename KPlusOneMer::multiple_hash,
			typename KPlusOneMer::equal_to> map_type;
	map_type nodes_;

	bool contains(const KPlusOneMer &k) const {
		return nodes_.find(k) != nodes_.end();
	}

	// DE BRUIJN:
	//does it work for primitives???
	void addEdge(const KPlusOneMer &k) {
		nodes_.insert(make_pair(k, make_pair(Value(), -1)));
	}

	void CountSequence(const Sequence& s) {
		if (s.size() >= size_) {
			Seq<size_> KPlusOneMer = s.start<size_> ();
			addEdge(KPlusOneMer);
			for (size_t j = size_; j < s.size(); ++j) {
				KPlusOneMer = KPlusOneMer << s[j];
				addEdge(KPlusOneMer);
			}
		}
	}

	void CountRead(const Read &read) {
		if (read.isValid()) {
			Sequence s = read.getSequence();
			CountSequence(s);
		}
	}

	// INDEX:

	void putInIndex(const KPlusOneMer &k, Value id, size_t offset) {
		map_iterator mi = nodes_.find(k);
		if (mi == nodes_.end()) {
			nodes_.insert(make_pair(k, make_pair(id, offset)));
		} else {
			mi->second.first = id;
			mi->second.second = offset;
		}
	}

public:
	typedef typename map_type::iterator map_iterator;
	typedef typename map_type::const_iterator map_const_iterator;

	// DE BRUIJN:

	SeqMap() {

	}

	template<class ReadStream>
	SeqMap(ReadStream &stream) {
		Fill<ReadStream>(stream);
	}

	template<class ReadStream>
	void Fill(ReadStream &stream) {
		Read r;
		while (!stream.eof()) {
			stream >> r;
			CountRead(r);
		}
	}

	map_iterator begin() {
		return nodes_.begin();
	}

	map_iterator end() {
		return nodes_.end();
	}

	// number of incoming edges for KPlusOneMer[1:]
	char IncomingEdgeCount(const KPlusOneMer &kPlusOneMer) {
		KPlusOneMer kPlusOneMer2 = kPlusOneMer << 'A';
		char res = 0;
		for (char c = 0; c < 4; ++c) {
			if (contains(kPlusOneMer2 >> c)) {
				res++;
			}
		}
		return res;
	}

	// number of outgoing edges for KPlusOneMer[:-1]
	char OutgoingEdgeCount(const KPlusOneMer &KPlusOneMer) {
		char res = 0;
		for (char c = 0; c < 4; ++c) {
			if (contains(KPlusOneMer << c)) {
				res++;
			}
		}
		return res;
	}

	KPlusOneMer NextEdge(const KPlusOneMer &kPlusOneMer) { // returns any next edge
		for (char c = 0; c < 4; ++c) {
			KPlusOneMer s = kPlusOneMer << c;
			if (contains(s)) {
				return s;
			}
		}
		assert(false); // no next edges (we should request one here).
	}

	// INDEX:

	bool containsInIndex(const KPlusOneMer &k) const {
		map_const_iterator mci = nodes_.find(k);
		return (mci != nodes_.end()) && (mci->second.second != (size_t) -1);
	}

	const pair<Value, size_t>& get(const KPlusOneMer &k) const {
		map_const_iterator mci = nodes_.find(k);
		assert(mci != nodes_.end()); // contains
		return mci->second;
	}

	bool deleteIfEqual(const KPlusOneMer &k, Value id) {
		map_iterator mi = nodes_.find(k);
		if (mi != nodes_.end() && mi->second.first == id) {
			nodes_.erase(mi);
			return true;
		}
		return false;
	}

	void RenewKmersHash(const Sequence& nucls, Value id) {
		assert(nucls.size() >= size_);
		KPlusOneMer k(nucls);
		putInIndex(k, id, 0);
		for (size_t i = size_, n = nucls.size(); i < n; ++i) {
			k = k << nucls[i];
			putInIndex(k, id, i - size_ + 1);
		}
	}

	void DeleteKmersHash(const Sequence& nucls, Value id) {
		assert(nucls.size() >= size_);
		KPlusOneMer k(nucls);
		deleteIfEqual(k, id);
		for (size_t i = size_, n = nucls.size(); i < n; ++i) {
			k = k << nucls[i];
			deleteIfEqual(k, id);
		}
	}

};

}

#endif /* SEQ_MAP_HPP_ */