package net.zerocontact.armor.modular.module.pouch.container.navboard;

public class NavBoardChunkTile {

    private final int[] pixelColors = new int[256];

    public void setPixel(int localX, int localZ, int color) {
        if(getPixel(localX,localZ)!=color){
            pixelColors[index(localX, localZ)] = color;
        }
    }

    public int getPixel(int localX, int localZ) {
        return pixelColors[index(localX,localZ)];
    }


    private int index(int x, int z) {
        return x + z * 16;
    }

}
