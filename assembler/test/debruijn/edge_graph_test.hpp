#ifndef EDGEGRAPHTEST_HPP_
#define EDGEGRAPHTEST_HPP_
#include "edge_graph_constructor.hpp"
#include "test_utils.hpp"
#include "cute.h"
#include <tr1/unordered_set>

namespace edge_graph {

using de_bruijn::Traversal;
using de_bruijn::DFS;
using de_bruijn::SimpleIndex;

void EmptyGraphTest() {
	EdgeGraph g(11);
	ASSERT_EQUAL(11, g.k());
	ASSERT_EQUAL(0u, g.size());
}

void OneVertexGraphTest() {
	EdgeGraph g(11);
	g.AddVertex();
	ASSERT_EQUAL(2u, g.size());
	Vertex *v = *(g.begin());
	Vertex *rcv = g.Complement(v);
	ASSERT(v != rcv);
	ASSERT_EQUAL(v, g.Complement(rcv));
}

pair<vector<VertexId> , vector<EdgeId> > createGraph(EdgeGraph &graph,
		int edgeNumber) {
	vector<VertexId> v;
	vector<EdgeId> e;
	v.push_back(graph.AddVertex());
	for (int i = 0; i < edgeNumber; i++) {
		v.push_back(graph.AddVertex());
		e.push_back(
				graph.AddEdge(v[v.size() - 2], v[v.size() - 1],
						Sequence("AAAAAAAAAAAAAAAAA")));
	}
	return make_pair(v, e);
}

void OneEdgeGraphTest() {
	EdgeGraph g(11);
	pair<vector<VertexId> , vector<EdgeId> > data = createGraph(g, 1);
	ASSERT_EQUAL(1u, g.OutgoingEdgeCount(data.first[0]));
	ASSERT_EQUAL(0u, g.OutgoingEdgeCount(data.first[1]));
	ASSERT_EQUAL(data.second[0], g.GetUniqueOutgoingEdge(data.first[0]));
	ASSERT_EQUAL(g.Complement(data.second[0]),
			g.GetUniqueOutgoingEdge(g.Complement(data.first[1])));
	ASSERT_EQUAL(data.second[0],
			g.Complement(g.Complement(data.second[0])));
	ASSERT_EQUAL(!(g.EdgeNucls(data.second[0])),
			g.EdgeNucls(g.Complement(data.second[0])));
}

void EdgeMethodsSimpleTest() {
	EdgeGraph g(11);
	pair<vector<VertexId> , vector<EdgeId> > data = createGraph(g, 2);
	ASSERT_EQUAL(data.second[0], &g.GetData(data.second[0]));
	ASSERT_EQUAL(
			true,
			g.AreLinkable(data.first[0], data.first[1],
					Sequence("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")));
	ASSERT_EQUAL(
			false,
			g.AreLinkable(data.first[0], data.first[1],
					Sequence("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC")));
}

void VertexMethodsSimpleTest() {
	EdgeGraph g(11);
	pair<vector<VertexId> , vector<EdgeId> > data = createGraph(g, 2);
	ASSERT_EQUAL(data.second[0], g.GetUniqueIncomingEdge(data.first[1]));
	ASSERT_EQUAL(data.second[0], g.GetUniqueOutgoingEdge(data.first[0]));
	ASSERT_EQUAL(false, g.CanCompressVertex(data.first[0]));
	ASSERT_EQUAL(true, g.CanCompressVertex(data.first[1]));
	ASSERT_EQUAL(false, g.CheckUniqueIncomingEdge(data.first[0]));
	ASSERT_EQUAL(true, g.CheckUniqueIncomingEdge(data.first[1]));
	ASSERT_EQUAL(false, g.CheckUniqueOutgiongEdge(data.first[2]));
	ASSERT_EQUAL(true, g.CheckUniqueOutgiongEdge(data.first[1]));
	ASSERT_EQUAL(true, g.IsDeadEnd(data.first[2]));
	ASSERT_EQUAL(false, g.IsDeadEnd(data.first[1]));
	ASSERT_EQUAL(true, g.IsDeadStart(data.first[0]));
	ASSERT_EQUAL(false, g.IsDeadStart(data.first[1]));
}

void GraphMethodsSimpleTest() {
	EdgeGraph g(11);
	pair<vector<VertexId> , vector<EdgeId> > data = createGraph(g, 2);
	ASSERT_EQUAL(vector<ActionHandler*> (), g.GetHandlers());
	ActionHandler* handler = new ActionHandler();
	g.AddActionHandler(handler);
	vector<ActionHandler*> handlers = g.GetHandlers();
	ASSERT_EQUAL(1u, handlers.size());
	ASSERT_EQUAL(handler, handlers[0]);
	g.RemoveActionHandler(handler);
	ASSERT_EQUAL(vector<ActionHandler*> (), g.GetHandlers());
}

void SmartIteratorTest() {
	EdgeGraph g(11);
	pair<vector<VertexId> , vector<EdgeId> > data = createGraph(g, 4);
	size_t num = 0;
	set<VertexId> visited;
	std::less<VertexId> comp;
	SmartVertexIterator<EdgeGraph> it = g.SmartVertexBegin(comp);
	SmartVertexIterator<EdgeGraph> it1 = g.SmartVertexBegin(comp);
	SmartVertexIterator<EdgeGraph> it2 = g.SmartVertexEnd(comp);
	SmartVertexIterator<EdgeGraph> it3 = g.SmartVertexEnd(comp);
	for (SmartVertexIterator<EdgeGraph> it = g.SmartVertexBegin(comp); g.SmartVertexEnd(
			comp) != it; ++it) {
		num++;
		visited.insert(*it);
	}
	ASSERT_EQUAL(num, data.first.size() * 2);
	for (size_t i = 0; i < data.first.size(); i++) {
		ASSERT(visited.find(data.first[i]) != visited.end());
		ASSERT(visited.find(g.Complement(data.first[i])) != visited.end());
	}
}

typedef tr1::unordered_set<string> edge_set;

string print(const edge_set& es) {
	string s = "Edge set : {";
	for (edge_set::const_iterator i = es.begin(); i != es.end(); ++i) {
		s += "'" + *i + "'; ";
	}
	return s;
}

class ToStringHandler: public TraversalHandler {
	edge_set& edges_;
public:
	ToStringHandler(edge_set& edges) :
		edges_(edges) {
	}

	virtual void HandleEdge(EdgeId e) {
		//todo rewrite using graph object (maybe add g_ to superclass)
		edges_.insert((*e).nucls().str());
	}

};

//todo refactor
void MyEquals(edge_set es, string s[], size_t length) {
	edge_set etalon_edges;
	for (size_t i = 0; i < length; ++i) {
		ASSERT(es.count(s[i]) == 1);
		ASSERT(es.count(complement(s[i])) == 1);
		etalon_edges.insert(s[i]);
		etalon_edges.insert(complement(s[i]));
	}
	ASSERT_EQUAL(etalon_edges.size(), es.size());
}

template<size_t kmer_size_>
void AssertGraph(size_t read_cnt, string reads_str[], size_t edge_cnt,
		string etalon_edges[]) {
	vector<Read> reads = MakeReads(reads_str, read_cnt);
	de_bruijn::DeBruijn<kmer_size_> debruijn;
	debruijn.ConstructGraph(reads);
	CondenseConstructor<kmer_size_> g_c(debruijn);
	EdgeGraph *g;
	SimpleIndex<6, Edge*> *index;
	g_c.ConstructGraph(g, index);

	edge_set edges;
	ToStringHandler h(edges);
	DFS<EdgeGraph> dfs(*g);
	dfs.Traverse(&h);

	DEBUG(print(edges));

	MyEquals(edges, etalon_edges, edge_cnt);

	delete g;
	delete index;
}

//todo rename tests

void TestSimpleThread() {
	static const size_t read_cnt = 1;
	string reads[read_cnt] = { "ACAAACCACCA" };
	static const size_t edge_cnt = 1;
	string edges[edge_cnt] = { "ACAAACCACCA" };
	AssertGraph<5> (read_cnt, reads, edge_cnt, edges);
}

void TestSimpleThread2() {
	static const size_t read_cnt = 2;
	string reads[read_cnt] = { "ACAAACCACCC", "AAACCACCCAC" };
	static const size_t edge_cnt = 1;
	string edges[edge_cnt] = { "ACAAACCACCCAC" };
	AssertGraph<5> (read_cnt, reads, edge_cnt, edges);
}

void TestSplitThread() {
	static const size_t read_cnt = 2;
	string reads[read_cnt] = { "ACAAACCACCA", "ACAAACAACCC" };
	static const size_t edge_cnt = 3;
	string edges[edge_cnt] = { "ACAAAC", "CAAACCACCA", "CAAACAACCC" };
	AssertGraph<5> (read_cnt, reads, edge_cnt, edges);
}

void TestSplitThread2() {
	static const size_t read_cnt = 2;
	string reads[read_cnt] = { "ACAAACCACCA", "ACAAACAACCA" };
	static const size_t edge_cnt = 4;
	string edges[edge_cnt] = { "AACCACCA", "ACAAAC", "CAAACCA", "CAAACAACCA" };
	AssertGraph<5> (read_cnt, reads, edge_cnt, edges);
}

void TestBuldge() {
	static const size_t read_cnt = 2;
	string reads[read_cnt] = { "ACAAAACACCA", "ACAAACCACCA" };
	static const size_t edge_cnt = 2;
	string edges[edge_cnt] = { "ACAAAACACCA", "ACAAACCACCA" };
	AssertGraph<5> (read_cnt, reads, edge_cnt, edges);
}

void TestCondenseSimple() {
	static const size_t read_cnt = 4;

	string reads[read_cnt] = { "CGAAACCAC", "CGAAAACAC", "AACCACACC",
			"AAACACACC" };
	static const size_t edge_cnt = 3;
	string edges[edge_cnt] = { "CGAAAACACAC", "CACACC", "CGAAACCACAC" };

	AssertGraph<5> (read_cnt, reads, edge_cnt, edges);
}

cute::suite EdgeGraphSuite() {
	cute::suite s;
	//	s.push_back(CUTE(EmptyGraphTest));
	//	s.push_back(CUTE(OneVertexGraphTest));
	//	s.push_back(CUTE(OneEdgeGraphTest));
	//	s.push_back(CUTE(EdgeMethodsSimpleTest));
	//	s.push_back(CUTE(VertexMethodsSimpleTest));
	//	s.push_back(CUTE(GraphMethodsSimpleTest));
	s.push_back(CUTE(SmartIteratorTest));
	//	s.push_back(CUTE(TestSimpleThread));
	//	s.push_back(CUTE(TestSimpleThread2));
	//	s.push_back(CUTE(TestSplitThread));
	//	s.push_back(CUTE(TestSplitThread2));
	//	s.push_back(CUTE(TestBuldge));
	//	s.push_back(CUTE(TestCondenseSimple));

	return s;
}
}

#endif /* EDGEGRAPHTEST_HPP_ */