package com.ceyoniq.gradle.buildprocess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

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
     * @param project the current project
     * @return version information from version.properties
     */
    public static VerInfo from( Project project ) {
        return from( project, project.getRootProject().file( "buildprocess/version.properties") );
    }

    /**
     * @param project the current project
     * @param versionFile the version file (default: "buildprocess/version.properties")
     * @return version information from version.properties
     */
    public static VerInfo from( Project project, File versionFile ) {

        var props = readVersionProperties( versionFile );
        var verInfo = new VerInfo( props );
        if ( project.getLogger().isInfoEnabled() ) {
            project.getLogger().info( "buildprocess/version.properties: {} - verInfo: {}", props, verInfo );
        }
        return verInfo;
    }

    private static Properties readVersionProperties( File file ) {
        if ( !file.exists() ) {
            throw new GradleException( "Version file $file.canonicalPath does not exists" );
        }

        final Properties versionProperties = new Properties();
        try ( final FileInputStream fis = new FileInputStream( file )) {
            versionProperties.load( fis );
        } catch ( IOException e ) {
            throw new GradleException( "Error reading file stream = " + file.getAbsolutePath(), e );
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
            this.branchName  = System.getenv( "CI_COMMIT_BRANCH" );
            
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
