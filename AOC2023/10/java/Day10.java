import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.Iterator;

public class Day10 {
	public static void main(String[] args) {
		String pathToData = Paths.get("AOC2023", "10", "DATA.txt").toAbsolutePath().normalize().toString();

		List<String> lines = new ArrayList<>();

		try {
			lines = Files.readAllLines(Paths.get(pathToData));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		PipeNetwork pipeNetwork = PipeNetwork.parse(lines);

		pipeNetwork.deleteNonLoops();

		System.out.println(pipeNetwork.getXYString());

		Set<List<Node>> loops = pipeNetwork.getSimpleLoops();
		List<Node> longestLoop = loops.stream().max((l1, l2) -> Integer.compare(l1.size(), l2.size())).get();

		PointInLoop pointInLoop = new PointInLoop(longestLoop);
		Map<Position, Integer> points = pointInLoop.getPoints();
		int inside = (int) points.values().stream().filter(i -> i == 1).count();

		System.out.println(pointInLoop);

		System.out.println("Task 1: " + longestLoop.size() / 2);

		System.out.println("Task 2: " + inside);

	}

	static record Position(int row, int col) {
	}

	static class PointInLoop {
		private Map<Position, Integer> points;
		private List<Node> loop;
		private Map<Position, Node> positionToNode;
		int maxRow;
		int maxCol;

		public PointInLoop(List<Node> loop) {
			this.loop = loop;
			this.points = new HashMap<>();
			this.positionToNode = new HashMap<>();
			for (int i = 0; i < loop.size(); i++) {
				Node node = loop.get(i);
				Position position = new Position(node.row(), node.col());
				positionToNode.put(position, node);
				points.put(position, 2);
			}
			maxRow = positionToNode.keySet().stream().mapToInt(Position::row).max().orElse(0); // more readable but
																								// less efficient
			maxCol = positionToNode.keySet().stream().mapToInt(Position::col).max().orElse(0);

			startRayMarcher();
		}

		private void startRayMarcher() {
			for (int i = -maxRow + 1; i < maxCol; i++) {
				rayMarch(i);
			}
		}

		private void rayMarch(int xStart) {
			// march in a 45 degree angle
			// start at the point where the ray either intersects the x axis or the y axis,
			// whichever is closer

			int y = 0;
			int x = xStart;
			if (xStart < 0) {
				y = -xStart;
				x = 0;
			}
			boolean inside = false;

			while (true) {
				Position position = new Position(y, x);
				if (positionToNode.containsKey(position)) {
					inside ^= isInverting(positionToNode.get(position));
				} else {
					points.put(position, inside ? 1 : 0);
				}
				if (y == maxRow || x == maxCol) {
					break;
				}
				y++;
				x++;
			}
		}

		private boolean isInverting(Node node) {
			Node prevNode = loop.get((loop.indexOf(node) - 1 + loop.size()) % loop.size());
			Node nextNode = loop.get((loop.indexOf(node) + 1) % loop.size());
			int count = 0; // if it's 0 or 2 it's a corner, where the ray doesn't pass through the area, it
							// just touches it
			if (prevNode.row() > node.row()) {
				count++;
			}
			if (nextNode.row() > node.row()) {
				count++;
			}
			if (prevNode.col() < node.col()) {
				count++;
			}
			if (nextNode.col() < node.col()) {
				count++;
			}
			if (count == 1 || count == 3) { // I put 3 here just to be safe
				return true;
			}
			return false;
		}

		public Map<Position, Integer> getPoints() {
			return points;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int row = 0; row <= maxRow; row++) {
				for (int col = 0; col <= maxCol; col++) {
					sb.append(switch (points.getOrDefault(new Position(row, col), 0)) {
						case 0 -> " ";
						case 1 -> "█";
						case 2 -> "░";
						default -> throw new RuntimeException("Invalid point value");
					});
				}
				sb.append('\n');
			}
			return sb.toString();
		}
	}

	static class PipeNetwork {
		private Graph graph;

		private Node startNode;

		public PipeNetwork(Graph graph, Node startNode) {
			this.graph = graph;
			this.startNode = startNode;
		}

		public static PipeNetwork parse(List<String> lines) {
			Graph graph = new Graph();
			Node startNode = null;

			for (int row = 0; row < lines.size(); row++) {
				String line = lines.get(row);
				for (int col = 0; col < line.length(); col++) {
					char value = line.charAt(col);
					PipeSegment pipeSegment = PipeSegment.fromChar(value);
					Node node = new Node(row, col);
					if (pipeSegment == PipeSegment.EMPTY) {
						continue;
					}
					if (pipeSegment == PipeSegment.START) {
						if (startNode != null) {
							throw new IllegalArgumentException("Multiple start nodes");
						}
						startNode = node;
					}
					List<Node> connectingNodes = getConnectingNodes(node, pipeSegment.getPossibleConnections());
					if (connectingNodes.size() == 0) {
						continue;
					}
					connectingNodes.forEach(node2 -> graph.addOneWayEdge(node, node2));
				}
			}

			return new PipeNetwork(graph, startNode);
		}

		public static List<Node> getConnectingNodes(Node node, PossibleConnections possibleConnections) {
			List<Node> connectingNodes = new ArrayList<>();

			if (possibleConnections.top) {
				int row = node.row() - 1;
				if (row >= 0) {
					connectingNodes.add(new Node(row, node.col()));
				}
			}

			if (possibleConnections.right) {
				connectingNodes.add(new Node(node.row(), node.col() + 1));
			}

			if (possibleConnections.bottom) {
				connectingNodes.add(new Node(node.row() + 1, node.col()));
			}

			if (possibleConnections.left) {
				int col = node.col() - 1;
				if (col >= 0) {
					connectingNodes.add(new Node(node.row(), col));
				}
			}

			return connectingNodes;
		}

		public List<List<Character>> getXYChars() {
			List<List<Character>> xyChars = new ArrayList<>();

			for (Node node : graph.adjList.keySet()) {
				while (xyChars.size() <= node.row()) {
					xyChars.add(new ArrayList<>());
				}
				List<Character> row = xyChars.get(node.row());
				while (row.size() <= node.col()) {
					row.add(' ');
				}
				row.set(node.col(),
						PipeSegment.fromPossibleConnections(node.getPossibleConnections(graph)).getCharToPrint());
			}

			if (startNode != null) {
				if (startNode.row() >= xyChars.size() || startNode.col() >= xyChars.get(startNode.row()).size()) {
					throw new IllegalArgumentException("Start node out of bounds");
				}
				xyChars.get(startNode.row()).set(startNode.col(), PipeSegment.START.getCharToPrint());
			}

			return xyChars;
		}

		public String getXYString() {
			List<List<Character>> xyChars = getXYChars();

			StringBuilder sb = new StringBuilder();
			for (List<Character> row : xyChars) {
				for (Character c : row) {
					sb.append(c);
				}
				sb.append('\n');
			}

			return sb.toString();
		}

		public void deleteNonLoops() {
			graph.removeOneWayEdges();
			graph.removeNodesWithEdgeCountBelow(2);
		}

		public Set<List<Node>> getSimpleLoops() {
			return graph.findSimpleLoops();
		}
	}

	static record PossibleConnections(boolean top, boolean right, boolean bottom, boolean left) {
	}

	static enum PipeSegment {
		EMPTY('.', false, false, false, false, ' '),
		VERTICAL('|', true, false, true, false, '│'),
		HORIZONTAL('-', false, true, false, true, '─'),
		TOP_LEFT('J', true, false, false, true, '┘'),
		TOP_RIGHT('L', true, true, false, false, '└'),
		BOTTOM_LEFT('7', false, false, true, true, '┐'),
		BOTTOM_RIGHT('F', false, true, true, false, '┌'),
		START('S', true, true, true, true, '█'),
		TOP_LEFT_RIGHT('@', true, true, false, true, '┴'),
		TOP_RIGHT_BOTTOM('@', true, true, true, false, '├'),
		BOTTOM_LEFT_RIGHT('@', false, true, true, true, '┬'),
		TOP_LEFT_BOTTOM('@', true, false, true, true, '┤'),
		TOP_LEFT_RIGHT_BOTTOM('S', true, true, true, true, '┼'),
		TOP('@', true, false, false, false, '╵'),
		RIGHT('@', false, true, false, false, '╶'),
		BOTTOM('@', false, false, true, false, '╷'),
		LEFT('@', false, false, false, true, '╴');

		private char value;
		private char charToPrint;
		private PossibleConnections possibleConnections;

		PipeSegment(char value, boolean top, boolean right, boolean bottom, boolean left, char charToPrint) {
			this.value = value;
			this.possibleConnections = new PossibleConnections(top, right, bottom, left);
			this.charToPrint = charToPrint;
		}

		public static PipeSegment fromChar(char value) {
			for (PipeSegment pipeSegment : PipeSegment.values()) {
				if (pipeSegment.value == value) {
					return pipeSegment;
				}
			}

			throw new IllegalArgumentException("Invalid pipe segment: " + value);
		}

		public static PipeSegment fromPossibleConnections(PossibleConnections possibleConnections) {
			for (PipeSegment pipeSegment : PipeSegment.values()) {
				if (pipeSegment.possibleConnections.equals(possibleConnections)) {
					return pipeSegment;
				}
			}

			throw new IllegalArgumentException("Invalid possible connections: " + possibleConnections);
		}

		public PossibleConnections getPossibleConnections() {
			return possibleConnections;
		}

		public char getCharToPrint() {
			return charToPrint;
		}
	}

	static class Graph {
		private Map<Node, List<Node>> adjList;

		public Graph() {
			adjList = new HashMap<>();
		}

		public void addVertex(Node vertex) {
			adjList.putIfAbsent(vertex, new LinkedList<>());
		}

		public void addEdge(Node source, Node destination) {
			this.addVertex(source);
			this.addVertex(destination);

			adjList.get(source).add(destination);
			adjList.get(destination).add(source);
		}

		public void addOneWayEdge(Node source, Node destination) {
			this.addVertex(source);
			this.addVertex(destination);

			adjList.get(source).add(destination);
		}

		public void removeVertex(Node vertex) {
			adjList.values().stream().forEach(e -> e.remove(vertex));
			adjList.remove(vertex);
		}

		public List<Node> getAdjVertices(Node vertex) {
			return adjList.get(vertex);
		}

		public void removeOneWayEdges() {
			for (Node vertex : new HashSet<>(adjList.keySet())) { // Create a copy to avoid
																	// ConcurrentModificationException
				List<Node> adjVertices = adjList.get(vertex);
				for (Iterator<Node> iterator = adjVertices.iterator(); iterator.hasNext();) {
					Node adjVertex = iterator.next();
					List<Node> adjAdjVertices = adjList.get(adjVertex);
					if (!adjAdjVertices.contains(vertex)) {
						iterator.remove();
					}
				}
			}
		}

		public void removeNodesWithEdgeCountBelow(int minEdgeCount) {
			while (true) {
				boolean removed = false;
				for (Node vertex : new HashSet<>(adjList.keySet())) { // Create a copy to avoid
																		// ConcurrentModificationException
					List<Node> adjVertices = adjList.get(vertex);
					if (adjVertices.size() < minEdgeCount) {
						removeVertex(vertex);
						removed = true;
						continue;
					}
				}
				if (!removed) {
					break;
				}
			}
		}

		public Set<List<Node>> findSimpleLoops() {
			List<Node> nodesLeft = new ArrayList<>(adjList.keySet());
			Set<List<Node>> loops = new HashSet<>();
			while (nodesLeft.size() > 0) {
				Node startNode = nodesLeft.get(0);
				List<Node> loop = findLoop(startNode);
				loops.add(loop);
				nodesLeft.removeAll(loop);
			}
			return loops;
		}

		private List<Node> findLoop(Node startNode) {
			List<Node> visited = new ArrayList<>();
			visited.add(startNode);
			while (true) {
				List<Node> adjVertices = adjList.get(startNode);
				if (adjVertices.size() != 2) {
					throw new RuntimeException("Not a simple loop");
				}
				Node nextNode = adjVertices.get(0);
				if (visited.contains(nextNode)) {
					nextNode = adjVertices.get(1);
					if (visited.contains(nextNode)) {
						return visited;
					}
				}
				visited.add(nextNode);
				startNode = nextNode;
			}
		}

	}

	static record Node(int row, int col) {
		public PossibleConnections getPossibleConnections(Graph graph) {
			List<Node> adjVertices = graph.adjList.get(this);
			boolean top = adjVertices.contains(new Node(row - 1, col));
			boolean right = adjVertices.contains(new Node(row, col + 1));
			boolean bottom = adjVertices.contains(new Node(row + 1, col));
			boolean left = adjVertices.contains(new Node(row, col - 1));
			return new PossibleConnections(top, right, bottom, left);
		}
	}
}
