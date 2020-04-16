package aga.android.ibeacon;

import java.util.List;

public interface IScanner {

    void setRegionDefinitions(List<RegionDefinition> definitions);

    void start();

    void stop();
}
