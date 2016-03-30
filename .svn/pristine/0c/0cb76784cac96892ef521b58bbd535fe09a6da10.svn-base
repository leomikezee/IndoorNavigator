package ca.uwaterloo.lab4_204_46;

import java.util.ArrayList;

import android.graphics.PointF;
import mapper.FloatHelper;

public class GraphNode{
	private PointF pos;
	private ArrayList<GraphNode> neighbours = new ArrayList<GraphNode>();
	
	public GraphNode(PointF p) {
		pos = p;
	}
	
	public void addNeighbour(GraphNode p) {
		neighbours.add(p);
	}
	
	public void removeNeighbour(GraphNode p) {
		neighbours.remove(p);
	}
	
	public float getDistance(GraphNode p) {
		return FloatHelper.distance(this.getPos(), p.getPos());
	}
	
	public PointF getPos() {
		return pos;
	}
	
	public ArrayList<GraphNode> getNeighbours() {
		return neighbours;
	}
}
