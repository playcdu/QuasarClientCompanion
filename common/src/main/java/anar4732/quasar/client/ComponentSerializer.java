package anar4732.quasar.client;

import com.google.gson.*;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Type;

public class ComponentSerializer implements JsonSerializer<Component>, JsonDeserializer<Component> {
	@Override
	public Component deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		return Component.Serializer.fromJson(json);
	}
	
	@Override
	public JsonElement serialize(Component src, Type typeOfSrc, JsonSerializationContext context) {
		return Component.Serializer.toJsonTree(src);
	}
}