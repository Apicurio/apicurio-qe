package apicurito.tests.utils.slenide;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;

import java.nio.file.Path;
import java.util.Arrays;

public class MavenUtils {
	private final Verifier maven;

	private MavenUtils(Verifier maven) {
		this.maven = maven;
		maven.setForkJvm(false);
	}

	public static MavenUtils forProject(Path projectPath) throws VerificationException {
		final Verifier verifier = new Verifier(projectPath.toAbsolutePath().toString(), true);
		return new MavenUtils(verifier);
	}

	public MavenUtils forkJvm() {
		maven.setForkJvm(true);

		// copy the DNS configuration
		if (System.getProperty("sun.net.spi.nameservice.nameservers") != null) {
			maven.setSystemProperty("sun.net.spi.nameservice.nameservers", System.getProperty("sun.net.spi.nameservice.nameservers"));
			maven.setSystemProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
			maven.setSystemProperty("sun.net.spi.nameservice.provider.2", "default");
		}
		return this;
	}

	public void executeGoals(String... goals) throws VerificationException {
		try {
			maven.executeGoals(Arrays.asList(goals));
		} finally {
			// always reset System.out and System.in streams
			maven.resetStreams();
		}
	}
}
