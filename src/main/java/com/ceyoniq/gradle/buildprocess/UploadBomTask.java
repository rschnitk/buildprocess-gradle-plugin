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
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

public class UploadBomTask extends DefaultTask {

    @InputFile
    private File bomFile;

    @Input
    private String uri; // "http://001linuxserver01.ct.com:8888/api/v1/bom"

    @Input
    private String apiKey;

    @Input
    private String projectUUID;

    public UploadBomTask() {
        setGroup( "BOM" );
    }
    
    @TaskAction
    public void uploadBom() throws InterruptedException {

        if ( bomFile == null ) throw new GradleException( "bom file not set");
        if ( apiKey == null ) throw new GradleException( "api key not set");
        if ( projectUUID == null) throw new GradleException( "project uuid not set");

        try {            
            String bom = Base64.getEncoder().encodeToString( Files.readAllBytes( this.bomFile.toPath() ) );
            String json = "{ \"project\": \"" + this.projectUUID + "\", \"bom\": \"" + bom + "\" }";
        
            var client = HttpClient.newBuilder().version( HttpClient.Version.HTTP_1_1 ).build();
        
            var request = HttpRequest.newBuilder()
                                     .uri( URI.create( this.uri ) )
                                     .header( "X-Api-Key", this.apiKey )
                                     .header( "Content-Type", "application/json" )
                                     .PUT( HttpRequest.BodyPublishers.ofString( json ) )
                                     .build();

            var response = client.send( request, HttpResponse.BodyHandlers.ofString() );

            if ( response.statusCode() != 200 ) {
                String body = response.body();
                throw new GradleException( "upload bom failed with error body: " + body.substring( 0, 80 ) );
            }
            getProject().getLogger().info( "BOM upload to {} successful.", this.uri );

        } catch ( IOException e ) {
            throw new GradleScriptException( "upload bom failed: " + e.getMessage(), e);
        }
        
    }

    public File getBomFile() {
        return bomFile;
    }

    public void setBomFile( File bomFile ) {
        this.bomFile = bomFile;
    }
    
    public String getUri() {
        return uri;
    }

    public void setUri( String url ) {
        this.uri = url;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey( String apiKey ) {
        this.apiKey = apiKey;
    }

    public String getProjectUUID() {
        return projectUUID;
    }

    public void setProjectUUID( String projectUUID ) {
        this.projectUUID = projectUUID;
    }
}
