package io.github.sullis.netty.playground;

import io.netty.util.ResourceLeakDetector.LeakListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NettyLeakListener implements LeakListener {
    private final List<String> leaks = new CopyOnWriteArrayList<>();

    @Override
    public void onLeak(String resourceType, String records) {
        leaks.add(resourceType);
    }

    public int getLeakCount() {
        return leaks.size();
    }

    public void assertZeroLeaks() {
        if (!leaks.isEmpty()) {
            throw new IllegalStateException("Netty leaks: " + leaks);
        }
    }
}