package apicurito.tests.configuration;

import lombok.Getter;

@Getter
public enum Component {

    //GENERATOR("fuse-apicurito-generator"),        //uncomment for template installation TODO
    //UI("apicurito-ui"),

    SERVICE("apicurito-service");

    private final String name;

    Component(String name) {
        this.name = name;
    }
}
