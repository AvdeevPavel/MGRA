/* 
 * seq_filter.hpp
 * 
 * Created on: 17.04.2011
 *     Author: Mariya Fomkina
 *   Modified: 18.04.2011 by author
 */

#ifndef _SEQ_FILTER_HPP_
#define _SEQ_FILTER_HPP_
 
#include "ireadstream.hpp"
#include "cuckoo.hpp"

template<size_t size>
class seq_filter {		
public:
	typedef cuckoo<Seq<size>, size_t, typename Seq<size>::multiple_hash, 
								 typename Seq<size>::equal_to, 4, 1000, 100, 6, 5> hm; 
	
	static void filter(const std::string& in, /*std::string& out,*/ 
										 const size_t L = 1) {
		std::vector<Read> reads = get_reads_from_file(in);
		hm map;
		add_seqs_from_reads_to_map(reads, map);
		write_seqs_from_map_to_stdout(map, L);
	}

	static std::vector<Seq<size> > filter(const std::vector<Read>& reads, 
																				const size_t L = 1) {
		hm map;
		add_seqs_from_reads_to_map(reads, map);
		return get_seqs_from_map(map, L);
	}

private:
	seq_filter();
	seq_filter(const seq_filter<size>& sf);

private:
	static void add_seqs_from_reads_to_map(const std::vector<Read>& reads, hm& map) {
		size_t cnt = reads.size();
		for (size_t i = 0; i < cnt; ++i) {
			add_seqs_from_read_to_map(reads[i], map);
		}
	}

	static std::vector<Read> get_reads_from_file(const std::string& in) {
		std::vector<Read>* reads = ireadstream::readAll(in);//, 10000);
		return *reads;
	}

	static void add_seqs_from_read_to_map(const Read& read, hm& map) {
		Sequence s = read.getSequence();
		if (s.size() >= size) {
			Seq<size> seq = s.start<size>();
			add_seq_in_map(seq, map);
			size_t s_size = s.size();
			for (size_t i = size; i < s_size; ++i) {
				Seq<size> next = seq << s[i];
				seq = next;
				add_seq_in_map(seq, map);
			}
		}
	}

	static void add_seq_in_map(Seq<size>& seq, hm& map) {
		typename hm::iterator it = map.find(seq);
		if (it == map.end()) {
			map.insert(std::make_pair(seq, 1));
		} else {
			(*it).second++;
		}
	}

	static std::vector<Seq<size> > get_seqs_from_map(hm& map, const size_t& L) {
		std::vector<Seq<size> > seqs;
		typename hm::iterator end = map.end();
		for (typename hm::iterator it = map.begin(); it != end; ++it) {
			if ((*it).second > L) seqs.push_back((*it).first);
		}
		return seqs;
	}

	static void write_seqs_from_map_to_stdout(hm& map, const size_t& L) {
		typename hm::iterator end = map.end();
		size_t cnt = 0;
		for (typename hm::iterator it = map.begin(); it != end; ++it) {
			if ((*it).second > L) {
				std::cout << (*it).first << std::endl;
				++cnt;
			}
		}
		std::cout << map.size() << " "  << cnt << " " << map.length() << " " 
							<< ((float)map.size())/map.length() << " " 
							<< ((float)map.size() - cnt)/map.size() << std::endl; //for test!
	}
};

#endif
