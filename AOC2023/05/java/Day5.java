import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day5 {
	public static void main(String[] args) {
		String pathToData = Paths.get("AOC2023", "05", "DATA.txt").toAbsolutePath().normalize().toString();

		List<String> lines = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(pathToData))) {
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		String target = "location";

		Plan plan = new Plan(lines);

		List<Converter> converters = plan.getConverters();
		List<Resource> resources = plan.getResources();
		List<Resource> convertedResources = resources.stream()
				.map(res -> {
					while (!res.getType().equals(target)) {
						converters.stream()
								.filter(converter -> converter.getSource().equals(res.getType()))
								.findFirst()
								.ifPresent(converter -> converter.convert(res));
					}
					return res;
				})
				.toList();
		long min = convertedResources.stream()
				.mapToLong(Resource::getNumber)
				.min()
				.orElseThrow();
		System.out.println("Task 1: " + min);

	}

	static class Plan {
		private List<Resource> resources;
		private List<Converter> converters;

		private static final Pattern RESOURCE_PATTERN = Pattern
				.compile("(?<identifier>\\w+)s:\\s(?<everythingElse>.*)");

		public Plan(List<String> lines) {
			resources = parseResources(lines.get(0));

			converters = parseConverters(
					// discard the first line, then group the rest by empty lines
					String.join("\n", lines.subList(1, lines.size())).split("\n\n"));
		}

		private List<Resource> parseResources(String resourceString) {
			List<Resource> resources = new ArrayList<>();

			Matcher matcher = RESOURCE_PATTERN.matcher(resourceString);

			if (matcher.find()) {
				String startingRes = matcher.group("identifier");
				String[] numberStrings = matcher.group("everythingElse").split(" ");

				Arrays.stream(numberStrings)
						.map(numberString -> new Resource(startingRes, Long.parseLong(numberString)))
						.forEach(resources::add);
			}

			return resources;
		}

		private List<Converter> parseConverters(String[] converterStrings) {
			List<Converter> converters = new ArrayList<>();

			for (String converterString : converterStrings) {
				Converter converter = new Converter(converterString);
				converters.add(converter);
			}

			return converters;
		}

		public List<Converter> getConverters() {
			return converters;
		}

		public List<Resource> getResources() {
			return resources;
		}
	}

	static class Resource {
		String type;
		Range range;

		public Resource(String type, long number) {
			this.type = type;
			setNumber(number);
		}

		public String getType() {
			return type;
		}

		public long getNumber() {
			return range.getStart();
		}

		public void setType(String type) {
			this.type = type;
		}

		public void setNumber(long number) {
			this.range = new Range(number, number);
		}
	}

	static class Converter {
		String source;
		String destination;

		TreeMap<Long, ConverterRange> ranges;

		private static final Pattern FROM_TO_PATTERN = Pattern
				.compile("(?<source>\\w+)-to-(?<destination>\\w+) map:\n(?<numbers>.*)", Pattern.DOTALL);

		// I'm assuming that ranges can't overlap as that wouldn't rly make sense
		// TODO: input validation
		public Converter(String input) {
			ranges = new TreeMap<>();

			Matcher matcher = FROM_TO_PATTERN.matcher(input);

			matcher.find();

			this.source = matcher.group("source");
			this.destination = matcher.group("destination");

			String[] numberStrings = matcher.group("numbers").split("\n");

			List<ConverterRange> ranges = new ArrayList<>();

			for (String numberString : numberStrings) {
				ranges.add(ConverterRange.fromString(numberString));
			}

			initMap(ranges);
		}

		public Converter(String source, String destination, List<ConverterRange> ranges) {
			this.source = source;
			this.destination = destination;
			this.ranges = new TreeMap<>();

			initMap(ranges);
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
			resource.setType(this.destination);
			resource.setNumber(convert(resource.getNumber()));
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
	}
}
