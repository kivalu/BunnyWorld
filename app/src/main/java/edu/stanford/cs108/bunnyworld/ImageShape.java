package edu.stanford.cs108.bunnyworld;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import static edu.stanford.cs108.bunnyworld.GameLoaderActivity.playing;
import static edu.stanford.cs108.bunnyworld.IntroScreenActivity.emulatorHeight;
import static edu.stanford.cs108.bunnyworld.IntroScreenActivity.emulatorWidth;

public class ImageShape extends Shape {
    //the file name
    private RectF scaledCoord;
    private boolean drawOriginalDim = false;

    //superclass constructor--- canvas refers to main canvas area for the editor
    public ImageShape(View view, RectF bounds, BitmapDrawable drawable, String txtString, int resourceId,
                      boolean visible, boolean movable, String name){
        super(view, bounds, drawable, txtString, resourceId, visible, movable, name);

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

        if(drawOriginalDim) {
            canvas.drawBitmap(image.getBitmap(), xPos, yPos, null);
        }
        else {
            float newWidth = scaledCoord.width()*width;
            float newHeight = scaledCoord.height()*height;
            RectF newBounds = new RectF(xPos, yPos, xPos+newWidth, yPos+newHeight);
            canvas.drawBitmap(image.getBitmap(), null, newBounds, null);
        }
    }

    //Editor activity calls this version of draw
    @Override
    public void draw(Canvas canvas) {
       // super.draw(canvas);
        float width = viewWidth;
        float height = viewHeight;
        float newX = scaledCoord.left*width;
        float newY = scaledCoord.top*height;
        Log.d("debug width", Float.toString(width));
        Log.d("debug height", Float.toString(height));
        Log.d("debug", Float.toString(newX));
        Log.d("debug", Float.toString(newY));
        if(drawOriginalDim) {
            Log.d("drawing", "drawing original shape " + name);
            canvas.drawBitmap(image.getBitmap(), newX, newY, null);
        }
        else {
            Log.d("drawing", "drawing scaled shape " + name);
            float newWidth = scaledCoord.width()*width;
            float newHeight = scaledCoord.height()*height;
            RectF newBounds = new RectF(newX, newY, newX+newWidth, newY+newHeight);
            canvas.drawBitmap(image.getBitmap(), null, newBounds, null);
        }
    }

    //functionality to draw original image
    public void drawOriginal(Canvas canvas){
        canvas.drawBitmap(image.getBitmap(), originalX, originalY, null);
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
