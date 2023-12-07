package com.hexagram2021.mod_whitelist.common.network;

import javax.annotation.Nullable;
import java.util.List;

public interface IPacketWithModIds {
	@Nullable
	List<String> getModIds();

	void setModIds(@Nullable List<String> modIds);
}
