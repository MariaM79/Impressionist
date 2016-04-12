package app.impressionist;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity  {

    ImageView alteredImageView;
    ImageView originalImageView;
    Button saveCanvasButtonDialog, saveCanvasButton, galleryButton, clearCanvasButton;
    Button undoCanvasButton, redoCanvasButton;
    CheckBox normalColorCheckBox;
    CheckBox negativeColorCheckBox;
    SeekBar opacitySeekBar, brushRadiusSeekBar;
    EditText nameEditTextDialog, descriptionEditTextDialog;

    OnTouchListener globalTouchListener;
    OnClickListener globalClickListener;
    SensorEventListener sensorListener;
    Bitmap originalBitmap, alteredBitmap;
    int originalBitmapHeight, originalBitmapWidth;
    int originalImageViewWidth, originalImageViewHeight;
    Brush brush;
    Canvas canvas;
    Dialog dialog;

    int COLOR_MODE = 1, NEGATIVE_MODE = 2, NORMAL_MODE = 1;
    float x0 = 0, y0 = 0, x1 = 0, y1 = 0;
    float paccelx = 0, paccely = 0;
    float currentx = 0, currenty = 0;
    int undoNumber = 0, maxundonumber = 0;
    String CACHE_FILES_FOLDER = "/cache";
    private int RESULT_LOADING_IMG = 1;


    void defineOnItemSelectedListener(Spinner spinner) {
        spinner.setOnItemSelectedListener(
                new OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selected = String.valueOf(parent.getItemAtPosition(position));

                        if (selected.equalsIgnoreCase("Square")) {
                            brush.style = Brush.BRUSH_TYPE_SQUARE;

                        } else if (selected.equalsIgnoreCase("Triangle")) {
                            brush.style = brush.BRUSH_TYPE_TRIANGLE;

                        } else if (selected.equalsIgnoreCase("Circle")) {
                            brush.style = Brush.BRUSH_TYPE_CIRCLE;

                        } else if (selected.equalsIgnoreCase("Splash")) {
                            brush.style = brush.BRUSH_TYPE_SPLASH;

                        } else if (selected.equalsIgnoreCase("Star")) {
                            brush.style = brush.BRUSH_TYPE_STAR;
                        }

//                        Toast.makeText(parent.getContext(), "Selected: " + selected, Toast.LENGTH_LONG).show();
                    }


                    public void onNothingSelected(AdapterView<?> parent) {
                        // TODO Auto-generated method stub
                    }
                }
        );
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        initializeDialog();
        initializeMainScreenElements();
        clearCacheFolder();
        setTitle("Impressionist Painter");

        defineGlobalTouchListener();
        defineGlobalClickListener();
        defineSeekBarChangeListener();

        brush = new Brush();
        brush.color = Color.GRAY;
        canvas = new Canvas();

        Spinner spinnerBrushStyle = (Spinner) findViewById(R.id.spinnerBrushStyle);
        defineOnItemSelectedListener(spinnerBrushStyle);

        List <String> brushStyleList = new ArrayList<String>();
        brushStyleList.add("Square");
        brushStyleList.add("Triangle");
        brushStyleList.add("Circle");
        brushStyleList.add("Splash");
        brushStyleList.add("Star");

        ArrayAdapter<String> brushStyleAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, brushStyleList);
        brushStyleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBrushStyle.setAdapter(brushStyleAdapter);
    }


    void defineGlobalTouchListener() {
        globalTouchListener = new OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                int id = view.getId();

                if (id == originalImageView.getId()) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        x0 = event.getX();
                        y0 = event.getY();
                        return true;
                    }

                    if (event.getAction() == MotionEvent.ACTION_MOVE && originalBitmap != null) {
                        float i = event.getX();
                        float j = event.getY();
                        float x, y;
                        x = i;
                        y = j;

                        if (x < originalBitmapWidth && x > 0 && y > 0 && y < originalBitmapHeight) {

                            if (!negativeColorCheckBox.isChecked()) {
                                brush.color = originalBitmap.getPixel((int) x, (int) y);
                                brush.updatePaint();
                                brush.draw(canvas, x, y);
                                alteredImageView.setImageBitmap(alteredBitmap);
                            } else {
                                brush.color = Math.abs(originalBitmap.getPixel((int) x, (int) y)) + Color.BLACK;
                                brush.updatePaint(brush.color, brush.opacity);
                                brush.draw(canvas, x, y);
                                alteredImageView.setImageBitmap(alteredBitmap);
                            }
                        }

                        x0 = x;
                        y0 = y;
                        return true;

                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        saveUndoData();
                    }

                } else if (id == alteredImageView.getId()) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        x0 = event.getX();
                        y0 = event.getY();
                        return true;
                    }

                    if (event.getAction() == MotionEvent.ACTION_MOVE && originalBitmap != null) {
                        float i = event.getX();
                        float j = event.getY();
                        float x, y;
//                        x = convertFromTouchToBitmapDimensionsX(i);
//                        y = convertFromTouchToBitmapDimensionsY(j);
                        x = i;
                        y = j;

//                        if (Math.abs(x - x0) > 2 || Math.abs(y - y0) > 2) {

                        if (x < originalBitmapWidth && x > 0 && y > 0 && y < originalBitmapHeight) {

//                            if (x < originalBitmapWidth && x > 0 && y > 0 && y < originalBitmapHeight) {
//                                brush.color = originalBitmap.getPixel((int) x, (int) y);
//                            }

                            if (!negativeColorCheckBox.isChecked()) {
                                brush.color = originalBitmap.getPixel((int) x, (int) y);
                                brush.updatePaint();
                                brush.draw(canvas, x, y);
                                alteredImageView.setImageBitmap(alteredBitmap);
                            } else {
                                brush.color = Math.abs(originalBitmap.getPixel((int) x, (int) y)) + Color.BLACK;
                                brush.updatePaint(brush.color, brush.opacity);
                                brush.draw(canvas, x, y);
                                alteredImageView.setImageBitmap(alteredBitmap);
                            }

//                        } else {
//                            if (event.getPointerCount() == 2) {
//                                brush.radius += 0.4f;
//                            }
//                            if (x < originalBitmapWidth && x > 0 && y > 0 && y < originalBitmapHeight) {
//                                brush.color = originalBitmap.getPixel((int) x, (int) y);
//                            }

                        }

                        x0 = x;
                        y0 = y;
                        return true;

                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        saveUndoData();
                    }
                }

                return false;
            }
        };

        originalImageView.setOnTouchListener(globalTouchListener);
        alteredImageView.setOnTouchListener(globalTouchListener);
    }


    void defineGlobalClickListener() {
        globalClickListener = new OnClickListener() {

            @Override
            public void onClick(View view) {
                int id = view.getId();

                if (id == galleryButton.getId()) {
                    getImageFromGallery();
//                    Toast.makeText(MainActivity.this, "Image loaded.", Toast.LENGTH_SHORT).show();

                } else if (id == saveCanvasButton.getId()) {
                    dialog.show();

                } else if (id == saveCanvasButtonDialog.getId()) {
                    String name, description;
                    name = nameEditTextDialog.getText().toString();
                    description = descriptionEditTextDialog.getText().toString();
                    saveCanvasToGallery(name, description);

                    dialog.dismiss();
                    Toast.makeText(MainActivity.this, "Image saved.", Toast.LENGTH_SHORT).show();

                } else if (id == clearCanvasButton.getId()) {
                    canvas.drawColor(Color.WHITE);
                    alteredImageView.setImageBitmap(alteredBitmap);
                    Toast.makeText(MainActivity.this, "Canvas cleared.", Toast.LENGTH_SHORT).show();

                } else if (id == normalColorCheckBox.getId()) {
                    negativeColorCheckBox.setChecked(false);
                    COLOR_MODE = NORMAL_MODE;

                } else if (id == negativeColorCheckBox.getId()) {
                    normalColorCheckBox.setChecked(false);
                    COLOR_MODE = NEGATIVE_MODE;

                } else if (id == undoCanvasButton.getId()) {
                    if (undoNumber > 0) {
                        readAndApplyUndoData();
                    }

                } else if (id == redoCanvasButton.getId()) {
                    if (undoNumber < maxundonumber - 1) {
                        readAndApplyRedoData();
                    }
                }
            }
        };

        galleryButton.setOnClickListener(globalClickListener);
        saveCanvasButton.setOnClickListener(globalClickListener);
        saveCanvasButtonDialog.setOnClickListener(globalClickListener);
        clearCanvasButton.setOnClickListener(globalClickListener);

        normalColorCheckBox.setOnClickListener(globalClickListener);
        negativeColorCheckBox.setOnClickListener(globalClickListener);

        undoCanvasButton.setOnClickListener(globalClickListener);
        redoCanvasButton.setOnClickListener(globalClickListener);
    }


    void initializeMainScreenElements() {
        originalImageView = (ImageView) findViewById(R.id.originalImageView);
        alteredImageView = (ImageView) findViewById(R.id.alteredImageView);

        galleryButton = (Button) findViewById(R.id.galleryButton);
        saveCanvasButton = (Button) findViewById(R.id.saveCanvasButton);
        clearCanvasButton = (Button) findViewById(R.id.clearCanvasButton);

        undoCanvasButton = (Button) findViewById(R.id.undoCanvasButton);
        redoCanvasButton = (Button) findViewById(R.id.redoCanvasButton);

        opacitySeekBar = (SeekBar) findViewById(R.id.opacitySeekBar);
        brushRadiusSeekBar = (SeekBar) findViewById(R.id.brushSizeSeekBar);
        normalColorCheckBox = (CheckBox) findViewById(R.id.normalBrushCheckBox);
        negativeColorCheckBox = (CheckBox) findViewById(R.id.negativeBrushCheckBox);

        normalColorCheckBox.setChecked(true);
    }


    void getImageFromGallery() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickIntent, RESULT_LOADING_IMG);
//        Toast.makeText(MainActivity.this, "Image Loaded.", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOADING_IMG && resultCode == RESULT_OK && data != null) {
            String imgDecodableString = "";
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(
                    selectedImage, filePathColumn, null, null, null
            );

            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            imgDecodableString = cursor.getString(columnIndex);
            cursor.close();

            originalBitmap = BitmapFactory.decodeFile(imgDecodableString);
            originalBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
            alteredBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

            originalBitmapHeight = originalBitmap.getHeight();
            originalBitmapWidth = originalBitmap.getWidth();

            canvas = new Canvas();
            canvas.setBitmap(alteredBitmap);
            canvas.drawColor(Color.WHITE);

            originalImageView.setImageBitmap(originalBitmap);
            alteredImageView.setImageBitmap(alteredBitmap);

            originalImageViewHeight = originalImageView.getMeasuredHeight();
            originalImageViewWidth = originalImageView.getWidth();
            saveUndoData();
            clearCacheFolder();

            currentx = originalBitmapWidth / 2;
            currenty = originalBitmapHeight / 2;
        }
    }


    void defineSeekBarChangeListener() {
        OnSeekBarChangeListener seekList = new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean bool) {
                int id = seekBar.getId();

                if (id == opacitySeekBar.getId()) {
                    brush.opacity = (int) ((float) progress * 2.5f);

                } else if (id == brushRadiusSeekBar.getId()) {
                    if (progress != 0) {
                        brush.radius = (float) progress / 2;
                        brush.radius = brush.radius * originalBitmapHeight / 300;
                    }
                }
            }


            @Override
            public void onStartTrackingTouch(SeekBar s) {
                // TODO: Implement this method
            }


            @Override
            public void onStopTrackingTouch(SeekBar s) {
                // TODO: Implement this method
            }
        };

        opacitySeekBar.setOnSeekBarChangeListener(seekList);
        brushRadiusSeekBar.setOnSeekBarChangeListener(seekList);
    }


    void initializeDialog() {
        dialog = new Dialog(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog);
        dialog.setTitle("Save Canvas to Gallery...");
        dialog.setContentView(R.layout.dialog);

        saveCanvasButtonDialog = (Button) dialog.findViewById(R.id.saveCanvasButtonDialog);
        nameEditTextDialog = (EditText) dialog.findViewById(R.id.nameEditTextDialog);
        descriptionEditTextDialog = (EditText) dialog.findViewById(R.id.descriptionEditTextDialog);
    }


    void saveCanvasToGallery(String fileName, String description) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(System.currentTimeMillis());
        MediaStore.Images.Media.insertImage(MainActivity.this.getContentResolver(), alteredBitmap, fileName + timeStamp + ".jpg", timeStamp.toString());
//        Toast.makeText(MainActivity.this, "Image saved.", Toast.LENGTH_SHORT).show();
    }


    void saveUndoData() {
        FileOutputStream fOutputStream = null;
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + CACHE_FILES_FOLDER);

        boolean success = true;

        if (!folder.exists()) {
            success = folder.mkdir();
        }

        if (success) {
            File file = new File(folder.getAbsolutePath() + "/" + Integer.toString(undoNumber) + ".jpeg");

            try {
                fOutputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                // TODO: Implement this method
            }

            if (alteredBitmap != null) {
                alteredBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOutputStream);
            }

            try {
                fOutputStream.flush();
                fOutputStream.close();
            } catch (IOException e) {
                // TODO: Implement this method
            }

            undoNumber++;

            if (maxundonumber < undoNumber) {
                maxundonumber = undoNumber;
            }
        }

    }


    void readAndApplyUndoData() {
        String photoPath = Environment.getExternalStorageDirectory() + File.separator;

        if (undoNumber > 1) {
            photoPath += CACHE_FILES_FOLDER + "/" + Integer.toString(undoNumber - 1) + ".jpeg";
            undoNumber--;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            alteredBitmap = BitmapFactory.decodeFile(photoPath, options);
            alteredBitmap = alteredBitmap.copy(Bitmap.Config.ARGB_8888, true);

            canvas.setBitmap(alteredBitmap);
            alteredImageView.setImageBitmap(alteredBitmap);
//            Toast.makeText(MainActivity.this, "Last action undone.", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(MainActivity.this, "Reached undo limit.", Toast.LENGTH_SHORT).show();
        }
    }


    void readAndApplyRedoData() {
        String photoPath = Environment.getExternalStorageDirectory() + File.separator;

        photoPath += CACHE_FILES_FOLDER + "/" + Integer.toString(undoNumber + 1) + ".jpeg";
        undoNumber++;

        File file = new File(photoPath);

        if (file.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            alteredBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            alteredBitmap = alteredBitmap.copy(Bitmap.Config.ARGB_8888, true);

            canvas.setBitmap(alteredBitmap);
            alteredImageView.setImageBitmap(alteredBitmap);
//            Toast.makeText(MainActivity.this, "Last action redone.", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(MainActivity.this, "Reached redo limit.", Toast.LENGTH_SHORT).show();
            undoNumber--;
        }
    }


    void clearCacheFolder() {
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + CACHE_FILES_FOLDER);
        File[] list = folder.listFiles();

        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                list[i].delete();
            }
        }
//        Toast.makeText(MainActivity.this, "Cache cleared.", Toast.LENGTH_SHORT).show();
    }



//        float convertFromTouchToBitmapDimensionsX(float touchx) {
//        float tx;
//        tx = touchx / originalImageViewWidth;
//        touchx = tx * originalBitmapWidth;
//        return touchx;
//    }
//
//    float convertFromTouchToBitmapDimensionsY(float touchy) {
//        float ty;
//        ty = touchy / originalImageViewHeight;
//        touchy = ty * originalBitmapHeight;
//        return touchy;
//    }

    void defineSensorListener() {
        sensorListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (COLOR_MODE == NEGATIVE_MODE && alteredBitmap != null) {
                    float ay = event.values[0];
                    float ax = event.values[1];

                    if (paccelx - ax < 0) {
                        currentx += 5;

                    } else if (ax > 0) {
                        currentx -= 5;
                    }

                    if (ay < 0) {
                        currenty += 5;

                    } else if (ay > 0) {
                        currenty -= 5;
                    }

                    if (currenty > originalBitmapHeight) {
                        currenty = originalBitmapHeight - 2;

                    } else if (currenty < 0) {
                        currenty = 0;
                    }

                    if (currentx > originalBitmapWidth) {
                        currentx = originalBitmapWidth - 1;

                    } else if (currentx < 0) {
                        currentx = 0;
                    }

                    brush.color = originalBitmap.getPixel((int) currentx, (int) currenty);
                    brush.updatePaint();
                    brush.draw(canvas, currentx, currenty);
                    alteredImageView.setImageBitmap(alteredBitmap);

                    paccelx = ax;
                    paccely = ay;
                }
            }


            @Override
            public void onAccuracyChanged(Sensor p1, int p2) {
                // TODO: Implement this method
            }
        };
    }

}
