package msu.cse476.hakalaja.project;

import androidx.core.content.FileProvider;

public class GarmntFileProvider extends FileProvider {
    public GarmntFileProvider() {
        super(R.xml.file_paths);
    }
}
