package com.ceyoniq.gradle.buildprocess;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

/**
 * UploadBom Task class
 */
public abstract class UploadBomTask extends DefaultTask {

    /**
     * Get the bom file
     * @return the bom file as RegularFileProperty
     */
    @InputFile
    public abstract RegularFileProperty getBomFile();

    /**
     * Get the uri
     * @return the uri for bom upload (e.g. "http://001linuxserver01.ct.com:8888/api/v1/bom" )
     */
    @Input
    public abstract Property<String> getUri();

    /**
     * Get dependency track api key
     * @return dependency track api key
     */
    @Input
    public abstract Property<String> getApiKey();

    /**
     * Get dependency track project uuid
     * @return dependency track project uuid
     */
    @Input
    public abstract Property<String> getProjectUUID();

    
    /**
     * task action for UploadBomTask
     * @throws InterruptedException on error
     */
    @TaskAction
    public void uploadBom() throws InterruptedException {

        try {            
            File bomFile = getBomFile().getAsFile().get();
            String bom = Base64.getEncoder().encodeToString( Files.readAllBytes( bomFile.toPath() ) );
            String json = "{ \"project\": \"" + getProjectUUID().get() + "\", \"bom\": \"" + bom + "\" }";
        
            var client = HttpClient.newBuilder().version( HttpClient.Version.HTTP_1_1 ).build();
        
            var request = HttpRequest.newBuilder()
                                     .uri( URI.create( getUri().get() ) )
                                     .header( "X-Api-Key", getApiKey().get() )
                                     .header( "Content-Type", "application/json" )
                                     .PUT( HttpRequest.BodyPublishers.ofString( json ) )
                                     .build();

            var response = client.send( request, HttpResponse.BodyHandlers.ofString() );

            if ( response.statusCode() != 200 ) {
                String body = response.body();
                throw new GradleException( "upload bom failed with error body: " + body.substring( 0, 80 ) );
            }
            getProject().getLogger().info( "BOM upload to {} successful.", getUri().get() );

        } catch ( IOException e ) {
            throw new GradleScriptException( "upload bom failed: " + e.getMessage(), e);
        }
        
    }
}
