package io.github.rschnitk.buildprocess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class VerInfo implements Serializable {

    /**
     * extension name of version info
     */
    public static final String EXT_KEY = "verInfo";

    private static final long serialVersionUID = -7560503114552007331L;

    private final String       majorMinor;
    private final String       component;
    private final String       version;
    private final String       releaseDate;

    private final String       fullVersion;
    
    private final String       buildID;
    private final String       commitID;
    private final String       branchName;

    // ------------------------------------------------------------------------------------

    /**
     * @param settings  the gradle settings object
     * @return version information from version.properties
     */
    public static VerInfo from( Settings settings ) {
        File verFile = new File(settings.getSettingsDir(), "buildprocess/version.properties" );
        return from( Logging.getLogger(Settings.class), verFile );
    }

    /**
     * @param project the current project
     * @return version information from version.properties
     */
    public static VerInfo from( Project project ) {
        File verFile = project.getRootProject().file( "buildprocess/version.properties");
        return from( project.getLogger(), verFile );
    }

    /**
     * @param logger the logger
     * @param versionFile the version file (default: "buildprocess/version.properties")
     * @return version information from version.properties
     */
    public static VerInfo from( Logger logger, File versionFile ) {

        var props = readVersionProperties( logger, versionFile );
        var verInfo = new VerInfo( props );
        if ( logger != null && logger.isInfoEnabled() ) {
            logger.info( "file {}: {} - verInfo: {}", versionFile, props, verInfo );
        }
        return verInfo;
    }

    private static Properties readVersionProperties( Logger logger, File file ) {
        final Properties versionProperties = new Properties();

        if ( !file.exists() ) {
            logger.warn( "Version file '{}' does not exists", file );
            return versionProperties;
        }
        try ( final FileInputStream fis = new FileInputStream( file )) {
            versionProperties.load( fis );
        } catch ( IOException e ) {
            logger.warn( "Error reading file stream = {}: {}", file.getAbsolutePath(), e.getMessage() );
        }
        return versionProperties;
    }

    // ------------------------------------------------------------------------------------
    
    private VerInfo( Properties props ) {

        this.majorMinor = props.getProperty( "major.minor.version" );
        this.component = props.getProperty( "component.version" );
        this.version = this.majorMinor + '.' + this.component;
        this.releaseDate = props.getProperty( "releasedate" );

        if ( props.getProperty( "commit.id" ) != null ) { // set by ABTS
            
            this.fullVersion = props.getProperty( "full.version" );
            this.buildID     = props.getProperty( "build.timestamp" );
            this.commitID    = props.getProperty( "commit.id" );
            this.branchName  = props.getProperty( "branch.name" );
            
        } else if ( System.getenv("GITLAB_CI") != null ) { // Gitlab
                        
            this.fullVersion = this.version + '.' + System.getenv( "CI_PIPELINE_ID" );
            this.buildID     = System.getenv( "CI_PIPELINE_ID" );
            this.commitID    = System.getenv( "CI_COMMIT_SHA" );
            this.branchName  = System.getenv( "CI_COMMIT_REF_NAME" );
            
        } else if ( System.getenv("GIT_COMMIT") != null ) { // Jenkins
            
            String revision = System.getenv( "BUILD_TIMESTAMP" ) != null ? System.getenv( "BUILD_TIMESTAMP" ) 
                                                                               : System.getenv( "BUILD_NUMBER" );

            this.fullVersion = this.version + '.' + revision;
            this.buildID     = System.getenv( "BUILD_NUMBER" );
            this.commitID    = System.getenv( "GIT_COMMIT" );
            this.branchName  = System.getenv( "BRANCH_NAME" );
            
        } else { // developer (local)
        
            this.fullVersion = this.version + ".0";
            this.buildID     = ( new SimpleDateFormat( "yyyyMMddHH" ) ).format( new Date() );
            this.commitID    = "git-sha1-hash";
            this.branchName  = "localbranch";
        }
    }


    // ------------------------------------------------------------------------------------

    /**
     * Get major.minor version
     * @return major.minor version
     */
    public String getMajorMinor() {
        return majorMinor;
    }

    /**
     * Get component version
     * @return component version
     */
    public String getComponent() {
        return component;
    }

    /**
     * Get version (major.minor + component)
     * @return version (major.minor + component)
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get release date
     * @return release date
     */
    public String getReleaseDate() {
        return releaseDate;
    }

    /**
     * Get full version
     * @return full version
     */
    public String getFullVersion() {
        return fullVersion;
    }

    /**
     * Get build id
     * @return build id
     */
    public String getBuildID() {
        return buildID;
    }

    /**
     * Get commit id
     * @return commit id
     */
    public String getCommitID() {
        return commitID;
    }

    /**
     * Get get branch name
     * @return
     */
    public String getBranchName() {
        return branchName;
    }

    @Override
    public String toString() {
        return "VerInfo [majorMinor=" + majorMinor + ", component=" + component + ", version=" + version + ", releaseDate=" + releaseDate + ", fullVersion="
                        + fullVersion + ", buildID=" + buildID + ", commitID=" + commitID + ", branchName=" + branchName + "]";
    }
}
