#ifndef COMMON_HPP_
#define COMMON_HPP_

#include <math.h>
#include <cstdio>
#include <iostream>
#include <vector>
#include <map>
#include <ext/hash_map>
#include <algorithm>
#include <string>
#define forn(i, n) for(int i = 0; i < (int) n; i++)
#define ll long long
#define pb push_back
#define mp make_pair
#define fi first
#define se second
#define edgesMap  map<ll, vector<VertexPrototype *> >
#define vertecesMap  map<ll, vector<VertexPrototype *> >


using namespace std;
const string parsed_reads = "data/reads_var_d.txt";
const string parsed_k_l_mers = "data/klmers_var_d.txt";
const string parsed_k_sequence = "data/vertices_var_d.txt";
const string error_log = "data/error.log";

const int k = 25;
const int l = 31;
const int readLength = 100;
const int maxSeqLength = 200;
#endif /*COMMON_HPP_*/
string decompress(ll a, int l);