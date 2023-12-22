import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.LongStream;

public class Day5 {
	public static void main(String[] args) {
		String pathToData = Paths.get("AOC2023", "05", "DATA.txt").toAbsolutePath().normalize().toString();

		List<String> lines = new ArrayList<>();

		try {
			lines = Files.readAllLines(Paths.get(pathToData));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		String target = "location";

		Plan task1 = Plan.parseTask1(lines);
		task1.convertUntil(target);
		System.out.println("Task 1: " + task1.getResource().getLowestNumber());

		Plan task2 = Plan.parseTask2(lines);
		task2.convertUntil(target);
		System.out.println("Task 2: " + task2.getResource().getLowestNumber());
	}

	static class Plan {
		private Resource resource;
		private Map<String, Converter> converters;

		private static final Pattern RESOURCE_PATTERN = Pattern
				.compile("(?<identifier>\\w+)s:\\s(?<everythingElse>.*)");

		public Plan(Resource resource, List<Converter> converters) {
			this.resource = resource;
			this.converters = new HashMap<>();

			for (Converter converter : converters) {
				this.converters.put(converter.getSource(), converter);
			}
		}

		public static Plan parseTask1(List<String> lines) {
			Resource resource = parseTask1Resource(lines.get(0));

			List<Converter> converters = parseConverters(
					// discard the first line, then group the rest by empty lines
					String.join("\n", lines.subList(1, lines.size())).split("\n\n"));

			return new Plan(resource, converters);
		}

		public static Plan parseTask2(List<String> lines) {
			Resource resource = parseTask2Resource(lines.get(0));

			List<Converter> converters = parseConverters(
					// discard the first line, then group the rest by empty lines
					String.join("\n", lines.subList(1, lines.size())).split("\n\n"));

			return new Plan(resource, converters);
		}

		private static record ResourceList(String type, List<Long> numbers) {
		}

		private static ResourceList parseResource(String resourceString) {
			List<Long> numbers = new ArrayList<>();

			Matcher matcher = RESOURCE_PATTERN.matcher(resourceString);

			if (matcher.find()) {
				String startingRes = matcher.group("identifier");
				String[] numberStrings = matcher.group("everythingElse").split(" ");

				Arrays.stream(numberStrings)
						.mapToLong(Long::parseLong)
						.forEach(numbers::add);

				return new ResourceList(startingRes, numbers);
			}
			return null;
		}

		private static Resource parseTask1Resource(String resourceString) {
			ResourceList resourceList = parseResource(resourceString);

			List<Range> ranges = resourceList.numbers().stream()
					.map(number -> new Range(number, number))
					.toList();

			return new Resource(resourceList.type(), ranges);
		}

		private static Resource parseTask2Resource(String resourceString) {
			ResourceList resourceList = parseResource(resourceString);

			List<Long> numbers = resourceList.numbers();

			if (numbers.size() % 2 != 0) {
				throw new RuntimeException("Uneven number of numbers");
			}

			List<Range> ranges = LongStream.range(0, numbers.size() / 2)
					.mapToObj(i -> {
						long start = numbers.get((int) (i * 2));
						long range = numbers.get((int) (i * 2 + 1));
						return new Range(start, start + range - 1);
					})
					.toList();

			return new Resource(resourceList.type(), ranges);
		}

		private static List<Converter> parseConverters(String[] converterStrings) {
			List<Converter> converters = new ArrayList<>();

			for (String converterString : converterStrings) {
				Converter converter = Converter.fromString(converterString);
				converters.add(converter);
			}

			return converters;
		}

		public Collection<Converter> getConverters() {
			return converters.values();
		}

		public Resource getResource() {
			return resource;
		}

		public void convertUntil(String target) {
			while (!resource.getType().equals(target)) {
				Converter converter = converters.get(resource.getType());
				try {
					converter.convert(resource);
				} catch (Exception e) {
					throw new RuntimeException("Failed to convert resource", e);
				}
			}
		}
	}

	static class Resource {
		String type;
		List<Range> ranges;

		public Resource(String type, List<Range> ranges) {
			this.type = type;
			this.ranges = ranges;
		}

		public String getType() {
			return type;
		}

		public long getLowestNumber() {
			return ranges.stream()
					.mapToLong(Range::getStart)
					.min()
					.orElseThrow(() -> new RuntimeException("No ranges"));
		}

		public void setType(String type) {
			this.type = type;
		}

		public List<Range> getRanges() {
			return ranges;
		}

		public void setRanges(List<Range> ranges) {
			this.ranges = ranges;
		}

		@Override
		public String toString() {
			return ranges.stream()
					.map(Range::toString)
					.reduce((a, b) -> a + "\n" + b)
					.orElse("");
		}
	}

	static class Converter {
		String source;
		String destination;

		TreeMap<Long, ConverterRange> ranges;

		private static final Pattern FROM_TO_PATTERN = Pattern
				.compile("(?<source>\\w+)-to-(?<destination>\\w+) map:\n(?<numbers>.*)", Pattern.DOTALL);

		public Converter(String source, String destination, List<ConverterRange> ranges) {
			this.source = source;
			this.destination = destination;
			this.ranges = new TreeMap<>();

			initMap(ranges);
		}

		public static Converter fromString(String input) {
			Matcher matcher = FROM_TO_PATTERN.matcher(input);

			matcher.find();

			String source = matcher.group("source");
			String destination = matcher.group("destination");

			String[] numberStrings = matcher.group("numbers").split("\n");

			List<ConverterRange> ranges = new ArrayList<>();

			Arrays.stream(numberStrings)
					.map(ConverterRange::fromString)
					.forEach(ranges::add);

			return new Converter(source, destination, ranges);
		}

		private void initMap(List<ConverterRange> ranges) {
			this.ranges = new TreeMap<>();

			for (ConverterRange range : ranges) {
				this.ranges.put(range.getSourceStart(), range);
			}
		}

		public long convert(long sourceNumber) {
			var entry = ranges.floorEntry(sourceNumber);
			if (entry == null) {
				return sourceNumber;
			}
			return entry.getValue().map(sourceNumber);
		}

		public void convert(Resource resource) {
			List<Range> convertedRanges = new ArrayList<>();
			for (Range range : resource.getRanges()) {
				// computer Overlap in the form of a list of ConverterRanges with each converter
				// range
				Long floorStart = ranges.floorKey(range.getStart());
				if (floorStart == null) {
					floorStart = range.getStart();
				}
				Collection<ConverterRange> relevantRanges = ranges.subMap(floorStart, true, range.getEnd(), true)
						.values();
				List<ConverterRange> overlaps = relevantRanges.stream()
						.map(converterRange -> range.getOverlap(converterRange))
						.filter(Objects::nonNull)
						.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

				// fill all gaps inside the resource range where no overlap exists with a
				// ConverterRange with offset 0
				List<ConverterRange> gaps = new ArrayList<>();
				long lastEnd = range.getStart();
				for (ConverterRange overlap : overlaps) {
					if (overlap.getStart() > lastEnd) {
						gaps.add(new ConverterRange(lastEnd, overlap.getStart() - 1, 0));
					}
					lastEnd = overlap.getEnd() + 1;
				}
				if (lastEnd < range.getEnd()) {
					gaps.add(new ConverterRange(lastEnd, range.getEnd(), 0));
				}

				// merge all overlaps and gaps into one list
				overlaps.addAll(gaps);

				// convert all ConverterRanges to normal Ranges by calling getDestinationRange()
				List<Range> destinationRanges = overlaps.stream()
						.map(ConverterRange::getDestinationRange)
						.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

				// merge all overlapping ranges
				List<Range> mergedDestinationRanges = new ArrayList<>();

				// Sort the ranges by their start value
				destinationRanges.sort(Comparator.comparingLong(Range::getStart));

				// Initialize with the first range, if available
				Range current = destinationRanges.isEmpty() ? null : destinationRanges.get(0);

				for (int i = 1; i < destinationRanges.size(); i++) {
					Range next = destinationRanges.get(i);
					if (current.isOverlapping(next)) {
						// Merge current and next ranges
						current = new Range(Math.min(current.getStart(), next.getStart()),
								Math.max(current.getEnd(), next.getEnd()));
					} else {
						// No overlap, add the current range to the list and move to the next range
						mergedDestinationRanges.add(current);
						current = next;
					}
				}

				// Add the last range if it exists
				if (current != null) {
					mergedDestinationRanges.add(current);
				}

				// add all merged ranges to the list of converted ranges
				convertedRanges.addAll(mergedDestinationRanges);
			}

			// set the new ranges of the resource
			resource.setRanges(convertedRanges);

			// set the new type of the resource
			resource.setType(destination);
		}

		public String getDestination() {
			return destination;
		}

		public String getSource() {
			return source;
		}

		public Collection<ConverterRange> getRanges() {
			return ranges.values();
		}
	}

	static class Range {
		private long start;
		private long end;

		public Range(long start, long end) {
			this.start = start;
			this.end = end;
		}

		public boolean isInRange(long value) {
			return (value >= start && value <= end);
		}

		public long getStart() {
			return start;
		}

		public long getEnd() {
			return end;
		}

		public boolean isOverlapping(Range other) {
			return (other.getStart() <= this.getEnd() && other.getEnd() >= this.getStart());
		}

		public ConverterRange getOverlap(ConverterRange other) {
			if (isOverlapping(other)) {
				long overlapStart = Math.max(this.getStart(), other.getStart());
				long overlapEnd = Math.min(this.getEnd(), other.getEnd());
				return new ConverterRange(overlapStart, overlapEnd, other.offset);
			}
			return null;
		}

		@Override
		public String toString() {
			return "[" + start + ", " + end + "]";
		}
	}

	static class ConverterRange extends Range {
		long offset;

		public ConverterRange(long sourceStart, long sourceEnd, long offset) {
			super(sourceStart, sourceEnd);
			this.offset = offset;
		}

		public static ConverterRange fromString(String line) {
			String[] numbers = line.split(" ");
			long destinationStart = Long.parseLong(numbers[0]);
			long sourceStart = Long.parseLong(numbers[1]);
			long range = Long.parseLong(numbers[2]);
			long offset = destinationStart - sourceStart;
			long sourceEnd = sourceStart + range - 1;
			return new ConverterRange(sourceStart, sourceEnd, offset);
		}

		public long map(long source) {
			if (isInRange(source)) {
				return source + offset;
			}
			return source;
		}

		public long getSourceStart() {
			return getStart();
		}

		public long getSourceEnd() {
			return getEnd();
		}

		public long getDestinationStart() {
			return getStart() + offset;
		}

		public long getDestinationEnd() {
			return getEnd() + offset;
		}

		public void addOffset(long offset) {
			this.offset += offset;
		}

		public ConverterRange getOverlap(ConverterRange other) {
			ConverterRange overlap = super.getOverlap(other);
			if (overlap != null) {
				overlap.addOffset(this.offset);
			}
			return overlap;
		}

		public Range getDestinationRange() {
			return new Range(getDestinationStart(), getDestinationEnd());
		}

		@Override
		public String toString() {
			return "[" + getSourceStart() + ", " + getSourceEnd() + "] -> [" + getDestinationStart() + ", "
					+ getDestinationEnd() + "]";
		}
	}
}
