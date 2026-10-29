package cl.bcs.normativa.cartolas.tools.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public final class DataGenerator {

    private static final Random RANDOM = new Random();

    private static String enclose(
            final String str) {
        return String.format("'%s'", str);
    }

    private static String format(
            final Date date) {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(date);
    }

    public static List<List<NameValue>> generateDummyData(
            final List<Field> fields,
            final int n) {
        final List<List<NameValue>> dummyData = new ArrayList<List<NameValue>>();
        for (int i = 0; i < n; i++) {
            final List<NameValue> items = generateDummyItem(fields);
            dummyData.add(items);
        }

        return dummyData;
    }

    private static List<NameValue> generateDummyItem(
            final List<Field> fields) {
        final List<NameValue> items = new ArrayList<NameValue>();
        for (final Field field : fields) {
            final String value = getRandomValue(field);
            final NameValue pair = new NameValue(field.getName(), value);
            items.add(pair);
        }

        return items;
    }

    private static BigDecimal getRandomBigDecimal() {
        final double x = 1000.0 + (100000.0 - 1000.0) * RANDOM.nextDouble();
        final BigDecimal y = new BigDecimal(x);
        return y.setScale(2, RoundingMode.HALF_UP);
    }

    private static Date getRandomDate() {
        final Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.YEAR, 2016);
        cal.set(Calendar.MONTH, getRandomInt(1, 12));
        cal.set(Calendar.DAY_OF_MONTH, getRandomInt(1, 25));
        return cal.getTime();
    }

    private static int getRandomInt(
            final int a,
            final int b) {
        return a + RANDOM.nextInt(b - a);
    }

    private static Integer getRandomInteger() {
        return RANDOM.nextInt(1000000);
    }

    private static String getRandomString() {
        return "TEXTO";
    }

    private static String getRandomValue(
            final Field field) {
        final String javaClass = field.getJavaClass();

        if ("String".equals(javaClass)) {
            return enclose(getRandomString());
        } else if ("Integer".equals(javaClass)) {
            return getRandomInteger().toString();
        } else if ("java.math.BigDecimal".equals(javaClass)) {
            return getRandomBigDecimal().toString();
        } else if ("java.sql.Timestamp".equals(javaClass)) {
            return enclose(format(getRandomDate()));
        } else {
            return "UNKNOWN TYPE";
        }
    }

    private DataGenerator() {
    }

}
