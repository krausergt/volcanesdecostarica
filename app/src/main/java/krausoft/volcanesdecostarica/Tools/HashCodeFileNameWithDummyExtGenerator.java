package krausoft.volcanesdecostarica.Tools;

import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;

import static java.lang.String.valueOf;

public class HashCodeFileNameWithDummyExtGenerator implements FileNameGenerator {
    @Override
    public String generate(String imageUri) {
        return valueOf(imageUri.hashCode()) + ".jpg";
    }
}
