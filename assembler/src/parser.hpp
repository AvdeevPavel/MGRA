#ifndef PARSER_HPP
#define PARSER_HPP

#include <string>
#include <zlib.h>
#include <stdio.h>
#include "libs/kseq/kseq.h"

// STEP 1: declare the type of file handler and the read() function 
KSEQ_INIT(gzFile, gzread)

class Read {
public:
	Read(const std::string &s);
	char operator[] (const int &index) const;
	std::string str() const;
public:
	char bytes[25]; // little-endian, max length: 100bp
};

class MatePair {
public:
	MatePair(const std::string &s1, const std::string &s2, const int id_);
	const int id; // consecutive number from input file :)
	const Read seq1, seq2;
};

class FASTQParser {
public:
	FASTQParser();
	void open(std::string filename1, std::string filename2); // open two files with mate paired reads
	MatePair read();
	bool eof();
	void close();
private:
	gzFile fp1, fp2;
	kseq_t *seq1, *seq2;
	bool eof_bit;
	bool opened; // ready for read
	int cnt;
	void do_read(); // do actual read, it's one sequence ahead of read()
};

#endif