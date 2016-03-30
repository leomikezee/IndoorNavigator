package ca.uwaterloo.lab4_204_46;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.graphics.PointF;
import mapper.FloatHelper;
import mapper.Mapper;

public class RouteFinder {
	
	// Abstract the map into graph
	static public ArrayList<GraphNode> makeGraph() {
     	ArrayList<GraphNode> graph = new ArrayList<GraphNode>();
        GraphNode v1 = new GraphNode(new PointF(3.5f, 18.5f));
       	GraphNode v2 = new GraphNode(new PointF(5.5f, 18.5f));
     	GraphNode v3 = new GraphNode(new PointF(12.5f, 18.5f));
      	GraphNode v4 = new GraphNode(new PointF(19, 18.5f));
      	GraphNode v5 = new GraphNode(new PointF(19, 5.5f));
     	GraphNode v6 = new GraphNode(new PointF(20.75f, 19.65f));
     	v1.addNeighbour(v2);
     	v2.addNeighbour(v1); v2.addNeighbour(v3);
     	v3.addNeighbour(v2); v3.addNeighbour(v4);
     	v4.addNeighbour(v3); v4.addNeighbour(v5); v4.addNeighbour(v6);
     	v5.addNeighbour(v4); v5.addNeighbour(v6);
     	v6.addNeighbour(v4); v6.addNeighbour(v5);
     	graph.add(0, v1); graph.add(1, v2); graph.add(2, v3);
     	graph.add(3, v4); graph.add(4, v5); graph.add(5, v6);
     	return graph;
	}
	
	// add new node to the graph
	static public GraphNode addNode(ArrayList<GraphNode> graph, PointF p) {
		GraphNode v = new GraphNode(p); graph.add(graph.size(), v);
		if (p.x < 5 && p.y > 19) {
            graph.get(0).getNeighbours().add(v);
            v.addNeighbour(graph.get(0));
        }
        else if (p.x > 3.5f && p.x < 7.5f && p.y < 17.5f) {
            graph.get(1).getNeighbours().add(v);
            v.addNeighbour(graph.get(1));
        }
        else if (p.x > 10.5f && p.x < 14.5f && p.y < 17.5f) {
            graph.get(2).getNeighbours().add(v);
            v.addNeighbour(graph.get(2));
        }
        else if (p.y > 17.5f && p.y < 19) {
            graph.get(0).getNeighbours().add(v); graph.get(1).getNeighbours().add(v);
            graph.get(2).getNeighbours().add(v); graph.get(3).getNeighbours().add(v);
            v.addNeighbour(graph.get(0)); v.addNeighbour(graph.get(1));
            v.addNeighbour(graph.get(2)); v.addNeighbour(graph.get(3));
        }
        else if (p.x > 17.75f && p.x < 22.75f && p.y < 20) {
            graph.get(3).getNeighbours().add(v);
            v.addNeighbour(graph.get(3));
        }
        else if (p.x > 22.75f && p.y < 7) {
            graph.get(4).getNeighbours().add(v);
            v.addNeighbour(graph.get(4));
        }
        else if (p.x > 22.75f && p.y > 19) {
            graph.get(5).getNeighbours().add(v);
            v.addNeighbour(graph.get(5));
        }
        else if (p.x > 19.5 && p.y > 20) {
            graph.get(5).getNeighbours().add(v);
            v.addNeighbour(graph.get(5));
        }
        return v;
	}
	
	// Get the node with lowest distance to the start node
	static public GraphNode min(ArrayList<GraphNode> Q, HashMap<GraphNode, Double> dist) {
		GraphNode result = Q.get(0);
		Double min = dist.get(result);
		for (int i = 0; i < Q.size(); i++) {
			GraphNode v = Q.get(i);
			if (dist.get(v) < min) {
				result = v;
				min = dist.get(result);
			}
		}
		return result;
	}
	
	// Dijkstra's Shortest Path Algorithm
	static public ArrayList<GraphNode> Dijkstra(ArrayList<GraphNode> graph, GraphNode start, GraphNode end) {
		ArrayList<GraphNode> path = new ArrayList<GraphNode>();
		ArrayList<GraphNode> unvisited = new ArrayList<GraphNode>();
		HashMap<GraphNode, GraphNode> previous = new HashMap<GraphNode, GraphNode>();
		HashMap<GraphNode, Double> distance = new  HashMap<GraphNode, Double>();
		for (int i = 0; i < graph.size(); i++) {
			GraphNode v = graph.get(i);
			unvisited.add(i, v);
			previous.put(v, null);
			distance.put(v, Double.POSITIVE_INFINITY);
		}
		distance.put(start, 0.0);
		while (unvisited.size() != 0) {
			GraphNode u = min(unvisited, distance);
			if (u != end) {
				unvisited.remove(u);
				for (GraphNode v : u.getNeighbours()) {
					Double alt = distance.get(u) + u.getDistance(v);
					if (alt < distance.get(v)) {
						distance.put(v, alt);
						previous.put(v, u);
					}
				}
			}
			else {
				break;
			}
		}
		GraphNode u = end;
		int count = 0;
		while (previous.get(u) != null) {
			path.add(count, u);
			u = previous.get(u);
			count++;
		}
		path.add(count, u);
		Collections.reverse(path);
		return path;
	}

	// Get the path according to the UserPoint and EndPoint on the map
	static public ArrayList<PointF> findPath(Mapper mapView) {
		ArrayList<PointF> path = new ArrayList<PointF>();
		PointF start = mapView.getUserPoint(); PointF end = mapView.getEndPoint();
		if (mapView.calculateIntersections(start, end).isEmpty()) {
			path.add(0, start); path.add(1, end);
		}
		else {
			ArrayList<GraphNode> graph = makeGraph();
			GraphNode startNode = addNode(graph, start); GraphNode endNode = addNode(graph, end);
			ArrayList<GraphNode> nodePath = Dijkstra(graph, startNode, endNode);
			for (int i = 0; i < nodePath.size(); i++) {
				path.add(i, nodePath.get(i).getPos());
			}
		}
		return path;
	}
	
	// Check if the UserPoint is near the Target Point
	static public boolean checkArrive(PointF p1, PointF p2) {
		return FloatHelper.distance(p1, p2) <= 1;
	}
	
	// Calculate the direction User should head to
	static public int calculateAngle(Mapper mapView, PointF next) {
		int result = 0;
        PointF p = new PointF(next.x, next.y + 1);
        if ((mapView.getUserPoint().x - next.x) < 0) {
        	float temp = FloatHelper.angleBetween(next, p, mapView.getUserPoint());
        	result = ((int) Math.toDegrees(temp) + 360) % 360;
        }
        else {
        	float temp = FloatHelper.angleBetween(next, p, mapView.getUserPoint());
        	result = 360 - ((int) Math.toDegrees(temp) + 360) % 360;
        }
        return result;
	}
}