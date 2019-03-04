package edu.stanford.cs108.bunnyworld;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class TextShape extends Shape {
    //the file name
    private String txtString;
    private Paint txtPaint = new Paint();
    private float textX;
    private float textY;

    //superclass constructor
    public TextShape(View view, String txtString, float strX, float strY,
                     boolean visible, boolean movable, String name){
        super(view, null, null, txtString, visible, movable, name);
        this.txtString = txtString;
        txtPaint.setColor(Color.BLACK);
        this.viewHeight = view.getHeight();
        this.viewWidth = view.getWidth();
        textX = strX/viewWidth;
        textY = strY/viewHeight;
    }

    //Called by any other canvas with new x and y positions for the object
    @Override
    public void draw(Canvas canvas, float xPos, float yPos) {
        canvas.drawText(txtString, xPos, yPos, txtPaint);
    }

    //called by any other canvas except the pageEditorView class
    @Override
    public void draw(Canvas canvas) {
        float width = canvas.getWidth();
        float height = canvas.getHeight();
        canvas.drawText(txtString, textX*width, textY*height, txtPaint);
    }

    //provide ability to change text properties
    public void changeText(String fontName, String fontSize, String fontStyle){
        //set boolean to true
        //create a new paint object and replace default paint
        // paint.setTypeface(); // takes plain/bold/DEFAULT_BOLD etc
        // paint.setTypeface(Typeface.create("Arial",Typeface.ITALIC));
        // paint.setTextSize(); //takes floating pt number
    }
}