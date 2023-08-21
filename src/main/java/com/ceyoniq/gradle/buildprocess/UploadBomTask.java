package com.ceyoniq.gradle.buildprocess;

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
 * UploadBom task class <p>
 * 
 * Usage: <code><pre>
    tasks.register("uploadBom", com.ceyoniq.gradle.buildprocess.UploadBomTask) {
       uri         = "http://001linuxserver01.ct.com:8888/api/v1/bom"
       bomFile     = new File(cyclonedxBom.destination.get(), 'bom.json')

       apiKey      = providers.gradleProperty('dtrack.api.key').get();
       projectUUID = providers.gradleProperty('dtrack.project.id').get()
       
       dependsOn cyclonedxBom
    }
 </pre></code> 
 */
public abstract class UploadBomTask extends DefaultTask {

    /**
     * Get BOM file as regular file property
     * @return BOM as RegularFileProperty
     */
    @InputFile
    public abstract RegularFileProperty getBomFile();

    /**
     * Get the URI of Dependency Track server
     * @return the URI for BOM upload (e.g. "http://001linuxserver01.ct.com:8888/api/v1/bom" )
     */
    @Input
    public abstract Property<String> getUri();

    /**
     * Get Dependency Track API key
     * @return Dependency Track API key
     */
    @Input
    public abstract Property<String> getApiKey();

    /**
     * Get Dependency Track project UUID
     * @return Dependency Track project UUID
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
            var bomFile = getBomFile().getAsFile().get();
            var bomBase64 = Base64.getEncoder().encodeToString( Files.readAllBytes( bomFile.toPath() ) );
            var json = "{ \"project\": \"" + getProjectUUID().get() + "\", \"bom\": \"" + bomBase64 + "\" }";
        
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
                if ( body.length() > 80 ) {
                    body = body.substring( 0, 80 );
                }
                throw new GradleException( "upload bom failed with error " + response.statusCode() + ", body: " + body );
            }
            getProject().getLogger().info( "BOM upload to {} successful.", getUri().get() );

        } catch ( IOException e ) {
            throw new GradleScriptException( "upload bom failed: " + e.getMessage(), e);
        }
        
    }
}
