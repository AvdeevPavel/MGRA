#ifndef HASH_HPP_
#define HASH_HPP_

#include "parameters.hpp"

template< typename T >
struct Hash {
public:
	unsigned int operator() (const T &seq) const {
	    unsigned int h = HASH_SEED;
		for (int i = 0; i < seq.size(); i++) {
			h = ((h << 5) - h) + seq[i];
		}
		return h;
	}
};

template< typename T >
struct HashSym {
public:
	unsigned int operator() (const T &seq) const {
		Hash<T> h;
		return h(seq) ^ h(!seq);
	};
};

template< typename T >
struct HashSymWeighted {
  unsigned int operator() (const T &seq) {
    // Will use frequency of seq
    return 0;
  }
};

#endif /* HASH_HPP_ */