#ifndef VISUALIZATIONUTILS_HPP_
#define VISUALIZATIONUTILS_HPP_

#include "edge_graph.hpp"
#include "coverage_counter.hpp"

namespace debruijn_graph {

class VisHandler: public TraversalHandler<EdgeGraph> {
	const EdgeGraph& g_;
	gvis::GraphPrinter<VertexId>& pr_;
public:

	VisHandler(const EdgeGraph& g, gvis::GraphPrinter<VertexId>& pr) :
		g_(g), pr_(pr) {
	}

	virtual void HandleVertex(VertexId v) {
		stringstream ss;
		ss << v<< " comp "<< g_.conjugate(v)<<"   ";
		pr_.addVertex(v, ss.str());
	}

	virtual void HandleEdge(EdgeId e) {
		stringstream ss;
		ss << e<<" comp "<<g_.conjugate(e)<<" len "<<g_.EdgeNucls(e).size()<<"   ";
		pr_.addEdge(g_.EdgeStart(e), g_.EdgeEnd(e), ss.str());
	}

};

class ConjugateVisHandler: public TraversalHandler<EdgeGraph> {
	const EdgeGraph& g_;
	gvis::PairedGraphPrinter<VertexId>& pr_;
	const map<EdgeId, string> color_;
	string ConstructLabel(EdgeId e) {
		stringstream ss;
//		if (g_.length(e) > 10)
			ss << g_.length(e);
//		else
//			ss << g_.length(e) << ":" << e->nucls();
		ss << "(";
		ss << ((int) (g_.coverage(e) * 100)) * 0.01;
		ss << ") id="<<e;
		return ss.str();
	}
public:

	ConjugateVisHandler(const EdgeGraph& g,
			gvis::PairedGraphPrinter<VertexId>& pr, map<EdgeId, string> color) :
		g_(g), pr_(pr), color_(color) {
	}

	ConjugateVisHandler(const EdgeGraph& g,
			gvis::PairedGraphPrinter<VertexId>& pr) :
		g_(g), pr_(pr), color_() {
	}

	virtual void HandleVertex(VertexId v) {
		pr_.addVertex(v, "", g_.conjugate(v), "");
	}

	virtual void HandleEdge(EdgeId e) {
		VertexId v1 = g_.EdgeStart(e);
		VertexId v2 = g_.EdgeEnd(e);
		map<EdgeId, string>::const_iterator col = color_.find(e);
		if (col == color_.end())
			pr_.addEdge(make_pair(v1, g_.conjugate(v1)),
					make_pair(v2, g_.conjugate(v2)), ConstructLabel(e));
		else
			pr_.addEdge(make_pair(v1, g_.conjugate(v1)),
					make_pair(v2, g_.conjugate(v2)), ConstructLabel(e),
					col->second);
	}

};

class GraphVisualizer {
public:
	virtual void Visualize(const EdgeGraph& g) = 0;
};

class SimpleGraphVisualizer: public GraphVisualizer {
	gvis::GraphPrinter<VertexId>& gp_;
public:
	SimpleGraphVisualizer(gvis::GraphPrinter<VertexId>& gp) :
		gp_(gp) {
	}

	virtual void Visualize(const EdgeGraph& g);
};

class ConjugateGraphVisualizer: public GraphVisualizer {
	gvis::PairedGraphPrinter<VertexId>& gp_;
public:
	ConjugateGraphVisualizer(gvis::PairedGraphPrinter<VertexId>& gp) :
		gp_(gp) {
	}

	virtual void Visualize(const EdgeGraph& g);
};

class ColoredPathGraphVisualizer: public GraphVisualizer {
	gvis::PairedGraphPrinter<VertexId>& gp_;
	const debruijn_graph::Path<EdgeId> path_;

	void SetColor(map<EdgeId, string> &color, Edge *edge, string col) {
		map<EdgeId, string>::iterator it = color.find(edge);
		if(it != color.end() && it->second != col) {
			color[edge] = "purple";
		} else
			color[edge] = col;
	}

	void constructColorMap(map<EdgeId, string> &color, const EdgeGraph &g, const Path<EdgeId> path) {
		for (Path<EdgeId>::iterator it = path.sequence().begin(); it
				!= path.sequence().end(); ++it) {
			SetColor(color, *it, "red");
			Edge* e = *it;
			EdgeId edge = g.conjugate(e);
			SetColor(color, edge, "blue");
		}
	}

public:
	ColoredPathGraphVisualizer(gvis::PairedGraphPrinter<VertexId>& gp,
			const debruijn_graph::Path<EdgeId> path) :
		gp_(gp), path_(path) {
	}

	virtual void Visualize(const EdgeGraph& g) {
		map<EdgeId, string> color;
		constructColorMap(color, g, path_);
		ConjugateVisHandler h(g, gp_, color);
		debruijn_graph::DFS<EdgeGraph>(g).Traverse(&h);
		gp_.output();
	}
};

void WriteToFile(const string& file_name, const string& graph_name,
		const EdgeGraph& g,
		Path<EdgeId> path = Path<EdgeId>());

}

#endif /* VISUALIZATIONUTILS_HPP_ */
