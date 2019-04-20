import java.util.Arrays;
import java.util.Date;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

class City extends Place implements Comparable, Serializable {
	protected Integer areaSize, x, y;
	protected OffsetDateTime initDate;

	public City() {
		this("Unnamed", 0);
	}

	public City(String name, Integer areaSize) {
		this(name, areaSize, 0, 0);
	}

	public City(String name, Integer areaSize, Integer x, Integer y) {
		super(name);
		initDate = OffsetDateTime.now();
		this.areaSize = areaSize;
		this.x = x;
		this.y = y;
	}

	public City(String name, Integer areaSize, Integer x, Integer y, Integer initDate) {
		super(name);
		this.initDate = OffsetDateTime.now();
		this.areaSize = areaSize;
		this.x = x;
		this.y = y;
	}

	public int getAreaSize() {
		return areaSize;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public long getInitDate() {
		return (long)initDate.toEpochSecond();
	}

	@Override
	public int compareTo(Object object) {
		if (this == object)
            return 0;

		if (object == null)
			throw new NullPointerException();

		if (this.getClass() != object.getClass())
			throw new ClassCastException();

		City city = (City)object;
		return this.areaSize.compareTo(city.areaSize) + this.name.compareTo(city.name);
	}

	@Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (object == null)
            return false;

        if (this.getClass() != object.getClass())
            return false;

        City city = (City)object;
        if (!this.name.equals(city.name) || !this.areaSize.equals(city.areaSize))
            return false;

        return true;
    }

    @Override
	public int hashCode() {
		return Arrays.deepHashCode((Object[])new Object[]{name, areaSize});
	}

    @Override
    public String toString() {
        return "Имя города: " + name + "\nПлощадь: " + areaSize + "\nКоординаты: " + x + ", " + y + "\nДата создания объекта: " + initDate.format(DateTimeFormatter.ofPattern("cccc, dd MMMM, yyyy kk:mm X"));
    }
}
