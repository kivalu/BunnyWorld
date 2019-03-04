package edu.stanford.cs108.bunnyworld;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.view.View;

public class ImageShape extends Shape {
    //the file name
    private Bitmap fileName;
    private RectF scaledCoord;
    private boolean drawOriginalDim = false;

    //superclass constructor--- canvas refers to main canvas area for the editor
    public ImageShape(View view, BitmapDrawable drawable, RectF bounds,
                      boolean visible, boolean movable, String name){
        super(view, bounds, drawable, null, visible, movable, name);
        this.fileName = drawable.getBitmap();

        //scale and store new image bounds for other canvas sizes
        float newX = originalX/viewWidth;
        float newY = originalY/viewHeight;
        float newWidth = bounds.width()/viewWidth;
        float newHeight = bounds.height()/viewHeight;
        scaledCoord = new RectF(newX, newY, newX + newWidth, newY + newHeight);
    }

    //override the shape draw
    @Override
    public void draw(Canvas canvas, float xPos, float yPos) {
        float width = canvas.getWidth();
        float height = canvas.getHeight();
        float newX = scaledCoord.left*width;
        float newY = scaledCoord.top*height;
        if(drawOriginalDim) canvas.drawBitmap(fileName, xPos, yPos, null);
        else {
            float newWidth = scaledCoord.width()*width;
            float newHeight = scaledCoord.height()*height;
            RectF newBounds = new RectF(xPos, yPos, xPos+newWidth, yPos+newHeight);
            canvas.drawBitmap(fileName, null, newBounds, null);
        }
    }

    //Editor activity calls this version of draw
    @Override
    public void draw(Canvas canvas) {
        float width = canvas.getWidth();
        float height = canvas.getHeight();
        float newX = scaledCoord.left*width;
        float newY = scaledCoord.top*height;
        if(drawOriginalDim) canvas.drawBitmap(fileName, newX, newY, null);
        else {
            float newWidth = scaledCoord.width()*width;
            float newHeight = scaledCoord.height()*height;
            RectF newBounds = new RectF(newX, newY, newX+newWidth, newY+newHeight);
            canvas.drawBitmap(fileName, null, newBounds, null);
        }
    }

    //functionality to draw original image
    public void drawOriginal(Canvas canvas){
        canvas.drawBitmap(fileName, originalX, originalY, null);
        drawOriginalDim = true;
    }

    //provide resize functionality
    public void resizeBounds(RectF newBounds){
        float newX = newBounds.left/viewWidth;
        float newY = newBounds.top/viewHeight;
        float newWidth = newBounds.width()/viewWidth;
        float newHeight = newBounds.height()/viewHeight;
        scaledCoord = new RectF(newX, newY, newX + newWidth, newY + newHeight);
    }
}