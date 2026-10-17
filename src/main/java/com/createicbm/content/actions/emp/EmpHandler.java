package com.createicbm.content.actions.emp;

import com.createicbm.config.blast.ConfigBlast;
import com.createicbm.config.util.BlockStateConfigList;

public class EmpHandler {
    public static final BlockStateConfigList.BlockChanceOut empBlockSwaps = new BlockStateConfigList.BlockChanceOut("[EmpReplacements]", (configList) -> {
        configList.load("create_icbm/blast/emp/replacements/list", ConfigBlast.emp.replacements.blockStates);
    });

    public static void loadFromConfig() {
        empBlockSwaps.reload();
    }

    public static void setup() {
        loadFromConfig();
    }
}
