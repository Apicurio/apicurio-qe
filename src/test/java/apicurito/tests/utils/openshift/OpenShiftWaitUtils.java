package apicurito.tests.utils.openshift;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class OpenShiftWaitUtils {

    private static final long DEFAULT_WAIT_INTERVAL = 1000L; // one second

    public static boolean isPodReady(Pod pod) {
        if (pod.getStatus().getConditions() != null) {
            Optional<PodCondition> readyCondition = pod.getStatus().getConditions().stream().filter(condition -> "Ready".equals(condition.getType())).findFirst();
            if (readyCondition.isPresent()) {
                return "True".equals(readyCondition.get().getStatus());
            }
        }

        return false;
    }

    public static boolean isPodRunning(Pod pod) {
        return "Running".equals(pod.getStatus().getPhase());
    }

    public static boolean hasPodRestartedAtLeastNTimes(Pod pod, int n) {
        if (pod.getStatus().getContainerStatuses() != null) {
            return pod.getStatus().getContainerStatuses().stream().filter(stats -> stats.getRestartCount() >= n).count() > 0;
        }

        return false;
    }

    public static boolean hasPodRestarted(Pod pod) {
        if (pod.getStatus().getContainerStatuses() != null) {
            return pod.getStatus().getContainerStatuses().stream().filter(stats -> stats.getRestartCount() > 0).count() > 0;
        }

        return false;
    }

    private static boolean _areExactlyNPodsRunning(Predicate<Pod> podFilter, int n) {
        return OpenShiftUtils.getInstance().getPods().stream().filter(podFilter).filter(OpenShiftWaitUtils::isPodRunning).count() == n;
    }

    private static boolean _areExactlyNPods(Predicate<Pod> podFilter, int n) {
        return OpenShiftUtils.getInstance().getPods().stream().filter(podFilter).count() == n;
    }

    public static BooleanSupplier areExactlyNPods(String podPartialName, int n) {
        return () -> _areExactlyNPods(pod -> pod.getMetadata().getName().contains(podPartialName), n);
    }

    private static boolean _areNPodsReady(Predicate<Pod> podFilter, int n) {
        return OpenShiftUtils.getInstance().getPods().stream().filter(podFilter).filter(OpenShiftWaitUtils::isPodReady).count() >= n;
    }

    private static boolean _areExactlyNPodsReady(Predicate<Pod> podFilter, int n) {
        return OpenShiftUtils.getInstance().getPods().stream().filter(podFilter).filter(OpenShiftWaitUtils::isPodReady).count() == n;
    }

    private static boolean hasAnyPodRestarted(Predicate<Pod> podFilter) {
        return OpenShiftUtils.getInstance().getPods().stream().filter(podFilter).filter(OpenShiftWaitUtils::hasPodRestarted).count() > 0;
    }

    public static boolean isAPodReady(Predicate<Pod> podFilter) {
        return _areNPodsReady(podFilter, 1);
    }

    public static BooleanSupplier areExactlyNPodsRunning(final String labelName, final String labelValue, int n) {
        return () -> _areExactlyNPodsRunning(pod -> labelValue.equals(pod.getMetadata().getLabels().get(labelName)), n);
    }

    public static BooleanSupplier areExactlyNPodsRunning(String appName, int n) {
        return () -> _areExactlyNPodsRunning(pod -> appName.equals(pod.getMetadata().getLabels().get("name")), n);
    }

    public static BooleanSupplier isAPodReady(String appName) {
        return () -> isAPodReady(pod -> appName.equals(pod.getMetadata().getLabels().get("name")));
    }

    public static BooleanSupplier isAPodReady(final String labelName, final String labelValue) {
        return () -> isAPodReady(pod -> labelValue.equals(pod.getMetadata().getLabels().get(labelName)));
    }

    public static BooleanSupplier areNPodsReady(String appName, int n) {
        return () -> _areNPodsReady(pod -> appName.equals(pod.getMetadata().getLabels().get("name")), n);
    }

    public static BooleanSupplier areNPodsReady(final String labelName, final String labelValue, int n) {
        return () -> _areNPodsReady(pod -> labelValue.equals(pod.getMetadata().getLabels().get(labelName)), n);
    }

    public static BooleanSupplier areExactlyNPodsReady(final String labelName, final String labelValue, int n) {
        return () -> _areExactlyNPodsReady(pod -> labelValue.equals(pod.getMetadata().getLabels().get(labelName)), n);
    }

    public static BooleanSupplier areExactlyNPodsReady(String appName, int n) {
        return () -> _areExactlyNPodsReady(pod -> appName.equals(pod.getMetadata().getLabels().get("name")), n);
    }

    public static BooleanSupplier areNPodsReady(Predicate<Pod> podFilter, int n) {
        return () -> _areNPodsReady(podFilter, n);
    }

    public static BooleanSupplier areNoPodsPresent(final String appName) {
        return () -> OpenShiftUtils.getInstance().getLabeledPods("component", appName).size() == 0;
    }

    public static BooleanSupplier areNoPodsPresent(Predicate<Pod> podFilter) {
        return () -> OpenShiftUtils.getInstance().getPods().stream().filter(podFilter).count() == 0;
    }

    public static BooleanSupplier hasPodRestarted(String appName) {
        return () -> hasAnyPodRestarted(pod -> appName.equals(pod.getMetadata().getLabels().get("name")));
    }

    public static BooleanSupplier hasPodRestarted(final String labelName, final String labelValue) {
        return () -> hasAnyPodRestarted(pod -> labelValue.equals(pod.getMetadata().getLabels().get(labelName)));
    }

    public static BooleanSupplier hasPodRestarted(Predicate<Pod> podFilter) {
        return () -> hasAnyPodRestarted(podFilter);
    }

    public static BooleanSupplier hasPodRestartedAtLeastNTimes(Predicate<Pod> podFilter, int n) {
        return () -> OpenShiftUtils.getInstance().getPods().stream().filter(podFilter).filter(p -> OpenShiftWaitUtils.hasPodRestartedAtLeastNTimes(p, n)).count() > 0;
    }

    public static BooleanSupplier conditionTrueForNIterations(BooleanSupplier condition, int iters) {
        final AtomicInteger ai = new AtomicInteger(0);

        return () -> {
            if (condition.getAsBoolean()) {
                int i = ai.incrementAndGet();
                return i >= iters;
            } else {
                ai.set(0);
                return false;
            }
        };
    }

    public static <X> boolean waitFor(Supplier<X> supplier, Function<X, Boolean> trueCondition, Function<X, Boolean> failCondition, long interval,
                                      long timeout) throws InterruptedException, TimeoutException {
        timeout = System.currentTimeMillis() + timeout;

        while (System.currentTimeMillis() < timeout) {

            X x = supplier.get();

            if (failCondition != null && failCondition.apply(x)) {
                return false;
            }

            if (trueCondition.apply(x)) {
                return true;
            }

            Thread.sleep(interval);
        }

        throw new TimeoutException();
    }

    public static boolean waitFor(BooleanSupplier condition, BooleanSupplier failCondition, long interval, long timeout)
            throws InterruptedException, TimeoutException {

        timeout = System.currentTimeMillis() + timeout;

        while (System.currentTimeMillis() < timeout) {

            if (failCondition != null && failCondition.getAsBoolean()) {
                return false;
            }

            if (condition.getAsBoolean()) {
                return true;
            }

            Thread.sleep(interval);
        }

        throw new TimeoutException();
    }

    public static boolean waitFor(BooleanSupplier condition, BooleanSupplier failCondition) throws InterruptedException, TimeoutException {
        return waitFor(condition, failCondition, DEFAULT_WAIT_INTERVAL, 5 * 60 * 1000);
    }

    public static void waitFor(BooleanSupplier condition) throws InterruptedException, TimeoutException {
        waitFor(condition, null, DEFAULT_WAIT_INTERVAL, 5 * 60 * 1000);
    }

    public static void waitFor(BooleanSupplier condition, long timeout) throws TimeoutException, InterruptedException {
        waitFor(condition, null, DEFAULT_WAIT_INTERVAL, timeout);
    }

    public static void assertEventually(String message, BooleanSupplier condition, long interval, long timeout) throws InterruptedException {
        try {
            waitFor(condition, null, interval, timeout);
        } catch (TimeoutException x) {
            throw new AssertionError(message, x);
        }
    }

    public static void assertEventually(String message, BooleanSupplier condition) throws InterruptedException {
        assertEventually(message, condition, DEFAULT_WAIT_INTERVAL, 5 * 60 * 1000);
    }

    public static void waitForPodIsReloaded(String podPartialName) throws InterruptedException, TimeoutException {
        Optional<Pod> pod = OpenShiftUtils.getPodByPartialName(podPartialName);
        assertThat(pod.isPresent()).isTrue();
        int currentNr = OpenShiftUtils.extractPodSequenceNr(pod.get());
        log.info("Current pod number: " + currentNr);
        int nextNr = currentNr + 1;

        String podPartialNextName = podPartialName + "-" + nextNr;
        log.info("Waiting for {} pod is reloaded", podPartialNextName);
        waitFor(() -> areExactlyNPods(podPartialNextName, 1).getAsBoolean());
        log.info("Pod {} is READY!", podPartialName);
    }

    /**
     * Method waits until pod with @param podPartialName appears
     * Build and deploy pods are ignored
     *
     * @param podPartialName pod partial name
     */
    public static void waitUntilPodAppears(String podPartialName) {
        try {
            waitFor(() -> isPodPresent(podPartialName), 5 * 60 * 1000);
        } catch (TimeoutException | InterruptedException e) {
            fail("Error thrown while checking if pod exists", e);
        }
    }

    private static boolean isPodPresent(String podPartialName) {
        Optional<Pod> integrationPod = OpenShiftUtils.getInstance().getPods().stream()
                .filter(p -> !p.getMetadata().getName().contains("build"))
                .filter(p -> !p.getMetadata().getName().contains("deploy"))
                .filter(p -> p.getMetadata().getName().contains(podPartialName)).findFirst();
        return integrationPod.isPresent();
    }
}
