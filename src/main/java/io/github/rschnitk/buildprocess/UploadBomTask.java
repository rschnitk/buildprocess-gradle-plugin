package io.github.rschnitk.buildprocess;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

/**
 * UploadBom task class <p>
 * 
 * Usage: <code><pre>
    tasks.register("uploadBom", com.ceyoniq.gradle.buildprocess.UploadBomTask) {
       uri         = "https://dependency-track-server/api/v1/bom"
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
     * @return the URI for BOM upload (e.g. "https://dependency-track-server/api/v1/bom" )
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
     * Trust all certificates?
     * @return true if check should be skipped
     */
    @Input @Optional
    public abstract Property<Boolean> getTrustAll();
        
    /**
     * Ignore upload error
     * @return true if ignore
     */
    @Input @Optional
    public abstract Property<Boolean> getIgnoreFailures();    
    
    /**
     * task action for UploadBomTask
     * @throws InterruptedException on error
     */
    @TaskAction
    public void uploadBom() throws InterruptedException, GeneralSecurityException {

        try {            
            var bomFile = getBomFile().getAsFile().get();
            var bomBase64 = Base64.getEncoder().encodeToString( Files.readAllBytes( bomFile.toPath() ) );
            var json = "{ \"project\": \"" + getProjectUUID().get() + "\", \"bom\": \"" + bomBase64 + "\" }";

            var sslContext = SSLContext.getDefault();
            if ( Boolean.TRUE.equals( getTrustAll().get() ) ) {
                sslContext = SSLContext.getInstance( "TLS" );
                sslContext.init( null, new TrustManager[] { new NonValidatingTM() }, new SecureRandom() );
            }

            var client = HttpClient.newBuilder()
                                   .version( HttpClient.Version.HTTP_1_1 )
                                   .sslContext( sslContext )
                                   .build();
            
            var request = HttpRequest.newBuilder()
                                     .uri( URI.create( getUri().get() ) )
                                     .header( "X-Api-Key", getApiKey().get() )
                                     .header( "Content-Type", "application/json" )
                                     .PUT( HttpRequest.BodyPublishers.ofString( json ) )
                                     .build();

            var response = client.send( request, HttpResponse.BodyHandlers.ofString() );

            if ( response.statusCode() != 200 ) {
                if ( ! getIgnoreFailures().get() ) {
                    String body = response.body();
                    if ( body.length() > 80 ) {
                        body = body.substring( 0, 80 );
                    }
                    throw new GradleException( "upload bom failed with error " + response.statusCode() + ", body: " + body );
                } else {
                    getProject().getLogger().warn( "BOM upload failed. Failure ignored." );
                }
            }
            getProject().getLogger().info( "BOM upload to {} successful.", getUri().get() );

        } catch ( IOException e ) {
            throw new GradleScriptException( "upload bom failed: " + e.getMessage(), e);
        }
    }

    private static class NonValidatingTM implements X509TrustManager {
        @Override
        @SuppressWarnings({"java:S1186", "java:S4830"})
        public void checkClientTrusted ( X509Certificate[] paramArrayOfX509Certificate, 
                                         String paramString ) throws CertificateException {
        }

        @Override
        @SuppressWarnings({"java:S1186", "java:S4830"})
        public void checkServerTrusted ( X509Certificate[] paramArrayOfX509Certificate, 
                                         String paramString ) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers () {
            return new X509Certificate [ 0 ];
        }
    }
}
