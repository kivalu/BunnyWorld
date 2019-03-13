package edu.stanford.cs108.bunnyworld;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PageEditorActivity extends AppCompatActivity implements BunnyWorldConstants {
    private Page page;
    private CustomPageView pagePreview;
    private EditText nameEditText, textEditText, xEditText, yEditText, wEditText, hEditText;
    private CheckBox visibleCheckBox, movableCheckBox;
    private HorizontalScrollView imgScrollView;
    private Spinner imgSpinner, verbSpinner, modifierSpinner, eventSpinner, actionSpinner;
    private LinearLayout actions, triggers;

    //array list of text shapes that is retrieved from EditPagesActivity
    private DatabaseHelper dbase;
    private int gameId;

    /**
     * Helper method that updates the Spinner to reflect the image clicked by the user
     */
    public static void updateSpinner(Spinner imgSpinner, String imgName) {
        ArrayAdapter<String> imgSpinnerAdapter = (ArrayAdapter<String>) imgSpinner.getAdapter();
        imgSpinner.setSelection(imgSpinnerAdapter.getPosition(imgName));
    }
    public static void populateSpinner(Spinner spinner, List<String> list) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                spinner.getContext(),
                android.R.layout.simple_spinner_item,
                list
        );
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(adapter);
    }
    /**
     * Event handler for when the "Save" button is clicked.
     */
    public void saveChanges(View view) {
        Shape selectedShape = pagePreview.getSelectedShape();
        if (selectedShape != null) {
            page.deleteShape(selectedShape);
            page.addShape(updatedShape());
            pagePreview.invalidate();
        }
    }
    public void addTriggerRow(View view) {
        addScriptRow(triggers, this::addTriggerRow, Arrays.asList(TRIGGER_EVENTS), this::deleteTriggerRow);
    }
    public void addActionRow(View view) {
        addScriptRow(actions, this::addActionRow, Arrays.asList(ACTION_VERBS), this::deleteActionRow);
    }
    public void deleteActionRow(View view) {
        deleteScriptRow(view);
    }
    public void deleteTriggerRow(View view) {
        deleteScriptRow(view);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_page);

        //initialize necessary UIs and helpers
        dbase = DatabaseHelper.getInstance(this);
        initComponents();
        populateSpinner(imgSpinner, dbase.getResourceNames());
        populateSpinner(verbSpinner, Arrays.asList(ACTION_VERBS));
        populateSpinner(eventSpinner, Arrays.asList(TRIGGER_EVENTS));
        initRightSpinner(verbSpinner, modifierSpinner, Arrays.asList(ACTION_VERBS));
        populateImgScrollView();

        //access the intents and use that to fill the page
        Intent intent = getIntent();
        page = extractIntentData(intent);
        initPageView();
    }

    /**
     * Helper method that initializes the relevant views defined in the editor_activity.xml
     * to be referred to later in the code.
     */
    private void initComponents() {
        nameEditText = findViewById(R.id.name);
        textEditText = findViewById(R.id.shapeText);
        xEditText = findViewById(R.id.rectX);
        yEditText = findViewById(R.id.rectY);
        wEditText = findViewById(R.id.width);
        hEditText = findViewById(R.id.height);
        visibleCheckBox = findViewById(R.id.visible);
        movableCheckBox = findViewById(R.id.movable);
        imgSpinner = findViewById(R.id.imgSpinner);
        verbSpinner = findViewById(R.id.verb1);
        modifierSpinner = findViewById(R.id.modifier1);
        eventSpinner = findViewById(R.id.event1);
        actionSpinner = findViewById(R.id.action1);
        actions = findViewById(R.id.actions);
        triggers = findViewById(R.id.triggers);
        imgScrollView = findViewById(R.id.presetImages);
        pagePreview = findViewById(R.id.pagePreview);
    }
    /**
     * Helper method that passes relevant data to PageView
     */
    private void initPageView() {
        pagePreview.setPage(page);
        String imgName = ((ArrayAdapter<String>)imgSpinner.getAdapter()).getItem(0);
        Bitmap newBitmap = dbase.getImage(imgName);
        //use the database to get the object
        BitmapDrawable defaultImage = new BitmapDrawable(newBitmap);
        pagePreview.setSelectedImage(defaultImage);
        //after loading page draw contents
        pagePreview.invalidate();
    }

    /**
     * Populates a HorizontalScrollView with all the preset images available for the user to create a shape out of
     */
    private void populateImgScrollView() {
        LinearLayout horizontalLayout = new LinearLayout(this);
        //get the corresponding images from the map
        for (String imgName : dbase.getResourceNames()) {
            ImageView imageView = new ImageView(this);
            //get the image from the database and create new drawable
            Bitmap imgBitmap = dbase.getImage(imgName);
            if(imgBitmap == null) Log.d("imgName", "Not found");
            imageView.setImageDrawable(new BitmapDrawable(imgBitmap));
            imageView.setOnClickListener(v -> {
                BitmapDrawable selectedImage = (BitmapDrawable) ((ImageView) v).getDrawable();
                pagePreview.setSelectedImage(selectedImage);
                updateSpinner(imgSpinner, imgName);
            });
            horizontalLayout.addView(imageView);
        }
        imgScrollView.addView(horizontalLayout);
    }
    private void initRightSpinner(Spinner leftSpinner, Spinner rightSpinner, List<String> leftSpinnerValues) {
        leftSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                populateSpinner(rightSpinner, getRightValues(leftSpinnerValues.get(position)));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
    private List<String> getRightValues(String leftValue) {
        return getModifierValues(leftValue);
    }
    private List<String> getModifierValues(String verb) {
        switch (verb) {
            case "goto": return dbase.getGamePageNames(page.getGameID());
            case "play": return Arrays.asList(AUDIO_NAMES);
            case "hide": case "show":
                List<Shape> allShapes = dbase.getPageShapes(page.getPageID(), pagePreview);
                List<String> shapeNames = new ArrayList<>();
                allShapes.forEach(shape -> shapeNames.add(shape.getName()));
                return shapeNames;
        }
        return new ArrayList<>();
    }
    private void addScriptRow(LinearLayout parentView, View.OnClickListener addRowListener, List<String> leftSpinnerValues, View.OnClickListener deleteRowListener) {
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

        LinearLayout scriptRow = new LinearLayout(this);
        scriptRow.setOrientation(LinearLayout.HORIZONTAL);
        scriptRow.setLayoutParams(rowParams);

        Spinner leftSpinner = new Spinner(this);
        populateSpinner(leftSpinner, leftSpinnerValues);
        leftSpinner.setLayoutParams(spinnerParams);
        Spinner rightSpinner = new Spinner(this);
        initRightSpinner(leftSpinner, rightSpinner, leftSpinnerValues);
        rightSpinner.setLayoutParams(spinnerParams);

        Button addScriptRow = new Button(this);
        addScriptRow.setLayoutParams(buttonParams);
        addScriptRow.setText("+");
        addScriptRow.setOnClickListener(addRowListener);
        Button deleteScriptRow = new Button(this);
        deleteScriptRow.setLayoutParams(buttonParams);
        deleteScriptRow.setText("-");
        deleteScriptRow.setOnClickListener(deleteRowListener);

        scriptRow.addView(leftSpinner);
        scriptRow.addView(rightSpinner);
        scriptRow.addView(addScriptRow);
        scriptRow.addView(deleteScriptRow);

        parentView.addView(scriptRow);
    }
    private void deleteScriptRow(View view) {
        LinearLayout scriptRow = (LinearLayout) view.getParent();
        LinearLayout parentView = (LinearLayout) scriptRow.getParent();
        if (parentView.getChildCount() > 1)
            parentView.removeView(scriptRow);
    }
    //writes the text-shapes into the ivar arrayList of text shapes above
    private Page extractIntentData(Intent intent){
        gameId = intent.getIntExtra("gameId", -1);
        //create a new page that has the properties of the previous page
        String pageName = intent.getStringExtra("pageName");
        if(!intent.getBooleanExtra("containsItems", false)){
            Page newPage = new Page(pageName);
            return newPage;
        }
        Page newPage = new Page(pageName);
        ArrayList<Integer> shapesId = intent.getIntegerArrayListExtra("ShapesArray");

        //instantiate the text-shapes ivar array
        ArrayList<Shape> shapes = new ArrayList<Shape>();
        //populate the shapes list
        for(int id: shapesId){
            ImageShape newShape = dbase.getShape(id, pagePreview);
            shapes.add(newShape);
        }
        newPage.setListOfShapes(shapes);
        return newPage;
    }

    /**
     * Creates a shape object using the attributes specified by the user.
     * Based on the image and text attributes, determines which subclass of Shape needs to be instantiated.
     * @return new Shape using the updated attributes
     */
    private Shape updatedShape() {
        String name = nameEditText.getText().toString();
        String text = textEditText.getText().toString();
        boolean visible = visibleCheckBox.isChecked();
        boolean movable = movableCheckBox.isChecked();

        String imageName = imgSpinner.getSelectedItem().toString();
        BitmapDrawable image = new BitmapDrawable(dbase.getImage(imageName));

        float x = Float.parseFloat(xEditText.getText().toString());
        float y = Float.parseFloat(yEditText.getText().toString());
        float width = Float.parseFloat(wEditText.getText().toString());
        float height = Float.parseFloat(hEditText.getText().toString());
        RectF boundingRect = new RectF(x, y, x + width, y + height);

        Shape shape;
        // When only image is provided
        if (image != null && text.isEmpty()) {
            //get the image id and pass it in
            int imgId = dbase.getId(RESOURCE_TABLE, imageName, NO_PARENT);
            shape = new ImageShape(pagePreview, boundingRect, image, text, imgId, visible, movable, name);
            // When text is provided, it takes precedence over any other object
        } else if (!text.isEmpty())
            shape = new TextShape(pagePreview, boundingRect, image, text, -1, visible, movable, name);
        // When neither image nor text is provided
        else
            shape = new RectangleShape(pagePreview, boundingRect, -1, visible, movable, name);

        return shape;
    }

    //save button method
    public void savePage(View view){
        //call the saveSelectedPage method
        if(pagePreview.getChangesMadeBool()){
            savePageBitmap(pagePreview);
            saveToDatabase();
        }
        pagePreview.setChangesMadeBool(false);
    }

    //undoes an action performed by the user on the screen
    public void undoChange(View view){
        //accesses the array list of actions and simply deletes the last activity
        boolean undo = pagePreview.undoChange();
        if(!undo) Toast.makeText(this, "Action undo successful", Toast.LENGTH_SHORT).show();
        pagePreview.setChangesMadeBool(false);
    }

    //redo button
    public void redoAction(View view) {
        //accesses the queue and simply adds that object to the arrayList
        boolean redo = pagePreview.redoAction();
        if(!redo) Toast.makeText(this, "Action redo successful", Toast.LENGTH_SHORT).show();
        pagePreview.setChangesMadeBool(false);
    }

    //on back pressed update the database by simply calling the save method
    //---FIXED
    @Override
    public void onBackPressed(){
        if(pagePreview.getChangesMadeBool()){
            AlertDialog.Builder alertBox = new AlertDialog.Builder(this)
                    .setTitle("Page Edit Changes")
                    .setMessage("Would you like to save changes?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            saveToDatabase();
                            pagePreview.setChangesMadeBool(false);
                            Toast.makeText(getApplicationContext(), "Changes saved", Toast.LENGTH_SHORT).show();
                            PageEditorActivity.super.onBackPressed();
                        }
                    });
            //add the no functionality
            alertBox.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    PageEditorActivity.super.onBackPressed();
                }
            }).create().show();
        }else super.onBackPressed();
    }

    //method that saves to the database
    public void saveToDatabase(){
        ArrayList<Shape> shapesList = pagePreview.getPageShapes();
        //saves all the shapes from the array list populated here
        String pageName = page.getName();

        String cmd = "SELECT * FROM pages WHERE name = '"+ pageName +"';";
        Cursor cursor = dbase.db.rawQuery(cmd, null);
        //delete old shapes and re-add new shapes
        int pageId = -1;
        if(cursor.getCount() != 0){
            cursor.moveToFirst();
            pageId = cursor.getInt(3);
            dbase.db.execSQL("DELETE FROM shapes WHERE parent_id = " + pageId + ";");
        }

        //else create the page and get the id for its children
        if(pageId == -1) {
            boolean success = dbase.addPage(pageName, page.getPageRender(), gameId);
            if(success) pageId = dbase.getId(PAGES_TABLE, pageName, gameId);
        }
        for(Shape currShape: shapesList){
            //name, parent_id, res_id, x, y, width, height, txtString, scripts, visible, movable
            String name = currShape.getName();
            String script;
            if(currShape.getScript() != null) script = currShape.getScript().toString();
            else script = "";
            String txtString = currShape.getText();
            if(txtString == null) txtString = "";
            int resId = currShape.getResId();
            dbase.addShape(name, pageId, resId, currShape.getX(), currShape.getY(), currShape.getWidth(),
                    currShape.getHeight(), txtString, script, currShape.isMovable(), currShape.isVisible());
        }

        //use the page Id to update the thumbnail
        dbase.changePageThumbnail(pageId, page.getPageRender());
    }

    //get the bitmap of the visible view
    private Bitmap getScreenView(View v){
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache(true);
        Bitmap newBitmap = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        return newBitmap;
    }

    //saves the bitmap to the page
    public void savePageBitmap(View view){
        //Bitmap renderToSave = getBitmapFromView(view);
        Bitmap renderToSave = getScreenView(view);
        page.setPageRender(renderToSave);
    }
}
