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

		List<List<Node>> loops = pipeNetwork.getSimpleLoops();

		System.out.println("Task 1: " +
				(loops.stream().mapToInt(List::size).max().getAsInt() / 2));
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

		public List<List<Node>> getSimpleLoops() {
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

		public List<List<Node>> findSimpleLoops() {
			List<Node> nodesLeft = new ArrayList<>(adjList.keySet());
			List<List<Node>> loops = new ArrayList<>();
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
