package io.dapas.prometheus.client.exporter;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is based on the Graphite class from the graphite-bridge in Prometheus simple client.
 *
 * The main difference is that this class only does formatting and makes no assumptions about transport
 * mechanisms. Also, it formats labels differently: it creates a new line per label, and shuffles the
 * order to create results that work better in Graphite / Grafana.
 *
 */
public class GraphiteFormat {

    private static final Pattern INVALID_GRAPHITE_CHARS = Pattern.compile("[^a-zA-Z0-9_-]");

    public static void write(Writer inputWriter, Enumeration<Collector.MetricFamilySamples> mfs) throws IOException {
        BufferedWriter writer = new BufferedWriter(inputWriter);
        Matcher m = INVALID_GRAPHITE_CHARS.matcher("");
        long now = System.currentTimeMillis() / 1000;
        for (Collector.MetricFamilySamples metricFamilySamples: Collections.list(mfs)) {
            for (Collector.MetricFamilySamples.Sample sample: metricFamilySamples.samples) {
                m.reset(sample.name);
                String prefix = m.replaceAll(".").replaceAll("_", ".");

                if ( sample.labelNames.isEmpty() ) {
                    writer.write(prefix);
                    writer.write(" " + sample.value + " " + now + "\n");
                } else {
                    for (int i = 0; i < sample.labelNames.size(); ++i) {
                        String label = sample.labelNames.get(i);
                        String value = sample.labelValues.get(i);
                        m.reset(value);

                        String text = label + "." + m.replaceAll("_").toLowerCase();

                        if ( prefix.contains(label)) {
                            writer.write(prefix.replaceAll(label, text));
                        } else {
                            writer.write(prefix + "." + text);
                        }
                        writer.write(" " + sample.value + " " + now + "\n");
                    }
                }
            }
        }
    }
}
