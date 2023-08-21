package com.ceyoniq.gradle.buildprocess;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Buildprocess plugin: add extension "verInfo" and registers uploadBom task
 */
public class BuildprocessPlugin implements Plugin< Project > {

    @Override
    public void apply( Project project ) {
        
        // version extension
        project.getExtensions().add( VerInfo.EXT_KEY, VerInfo.from( project ) );
    }
}
