package io.github.rschnitk.buildprocess;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Buildprocess plug-in: add extension "verInfo"
 */
public class BuildprocessPlugin implements Plugin< Project > {

    @Override
    public void apply( Project project ) {
        
        // version extension
        project.getExtensions().add( VerInfo.EXT_KEY, VerInfo.from( project ) );
    }
}
