package cloud.fogbow.fns.api.http;

import cloud.fogbow.fns.core.ApplicationFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.service.Contact;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
public class SwaggerConfiguration {

    public static final String BASE_PACKAGE = "cloud.fogbow.fns";
    public static final String API_TITLE = "Fogbow Federated Network Service (FNS) API";
    public static final String API_DESCRIPTION = "This document introduces Fogbow's FNS REST API, " +
            "provides guidelines on how to use it, and describes the available features accessible from it. " +
            "Fogbow's FNS extends Fogbow's Resource Allocation Service (RAS) API. It allows the creation of " +
            "federated networks spanning multiple providers. It also allows the creation of compute instances " +
            "that can be attached to the federated networks created. The FNS also works as a proxy for the" +
            "underlying RAS service, forwarding requests that it cannot handle to the underlying RAS.";

    public static final String CONTACT_NAME = "Fogbow";
    public static final String CONTACT_URL = "https://www.fogbow.cloud";
    public static final String CONTACT_EMAIL = "contact@fogbow.cloud";
    public static final Contact CONTACT = new Contact(
        CONTACT_NAME,
        CONTACT_URL,
        CONTACT_EMAIL);

    @Bean
    public Docket apiDetails() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2);
        docket.select()
            .apis(RequestHandlerSelectors.basePackage(BASE_PACKAGE))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(this.apiInfo().build());

        return docket;
    }

    private ApiInfoBuilder apiInfo() {
        String versionNumber = ApplicationFacade.getInstance().getVersionNumber();

        ApiInfoBuilder apiInfoBuilder = new ApiInfoBuilder();

        apiInfoBuilder.title(API_TITLE);
        apiInfoBuilder.description(API_DESCRIPTION);
        apiInfoBuilder.version(versionNumber);
        apiInfoBuilder.contact(CONTACT);

        return apiInfoBuilder;

    }

}
