package aga.android.ibeacon;

import java.util.List;

public interface IScanner {

    void setBeaconListener(IBeaconListener listener);

    void setRegionDefinitions(List<RegionDefinition> definitions);

    void start();

    void stop();
}
