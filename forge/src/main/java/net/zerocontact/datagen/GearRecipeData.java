package net.zerocontact.datagen;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GearRecipeData {
    @SerializedName("recipes")
    public List<GearRecipeData> recipes;
    @SerializedName("gear_id")
    public String gearId;
    @SerializedName("ingredient_items")
    public List<IngredientItems> ingredientItems;
    public GearRecipeData(String gearId, List<IngredientItems> ingredientItems){
        this.gearId = gearId;
        this.ingredientItems = ingredientItems;
    }
    public static class IngredientItems {
        public String itemId;
        @SerializedName("count")
        public int neededCount;
    }
}
