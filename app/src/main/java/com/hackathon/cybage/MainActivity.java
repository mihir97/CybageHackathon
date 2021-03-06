package com.hackathon.cybage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONException;

public class MainActivity extends Activity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback, LocationListener, AsyncTaskComplete {
    private LocationManager locationManager;
    private String provider;
    private GoogleMap mMap;
    private LatLng latLng;
    private FloatingActionButton addMarker;
    private Location location;
    private ActionHandler actionHandler;
    private float accuracy;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        actionHandler = new ActionHandler(MainActivity.this, this);
        addMarker = (FloatingActionButton) findViewById(R.id.addMarkerFAB);
        addMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (accuracy > 8) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Poor GPS Accuracy")
                            .setMessage("Please try after some time, ensure you are outdoor.\nCurrent Accuracy:" + location.getAccuracy())
                            .setPositiveButton("Try Later", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Mark new dustbin")
                            .setMessage("The current location will be marked as a spot with a dustbin.")
                            .setPositiveButton("Mark Now", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    actionHandler.addLocation(latLng.latitude, latLng.longitude);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        });

        MapFragment mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        }
    }

    /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        locationManager.removeUpdates(this);
    }


    @Override
    public void onLocationChanged(Location location) {
        double lat = (location.getLatitude());
        double lng = (location.getLongitude());
        latLng = new LatLng(lat, lng);

        if (mMap != null) {
            goToLatLng(latLng);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);

        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        if (latLng == null)
            latLng = new LatLng(18, 73);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(17.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.moveCamera(cameraUpdate);
        goToLatLng(latLng);
        actionHandler.fetchLocations(latLng.latitude, latLng.longitude);
    }

    void goToLatLng(LatLng latLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void handleResult(JsonObject result, String action) throws JSONException {
        if (result.get("success").getAsInt() == -1) {
            //TODO : Alert box
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Server not Reachable")
                    .setMessage("Make sure you are connceted to the internet!")
                    .setPositiveButton("Try Later", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        if (action.equals("Add") && result.get("success").getAsInt() == 1) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Thank You")
                    .setMessage("Thank you for contributing to the society !")
                    .setPositiveButton("Mark More", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mMap.addMarker(new MarkerOptions().position(latLng)
                                    .title("Use Me")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                            );
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        if (action.equals("Add") && result.get("success").getAsInt() == 0) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Duplicate")
                    .setMessage("This location is already registered.")
                    .setPositiveButton("Mark More", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        if (action.equals("Fetch") && result.get("success").getAsInt() == 1) {
            JsonArray jsonArray = result.getAsJsonArray("locations");
            for (int i = 0; i < jsonArray.size(); i++) {
                Drawable d = getResources().getDrawable(R.drawable.marker);
                BitmapDrawable bd = (BitmapDrawable) d.getCurrent();
                Bitmap b = bd.getBitmap();
                Bitmap resized = Bitmap.createScaledBitmap(b, 70, 80, false);

                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(jsonArray.get(i).getAsJsonObject().get("latitude").getAsDouble(), jsonArray.get(i).getAsJsonObject().get("longitude").getAsDouble()))
                        .title("Use Me")
                        .icon(BitmapDescriptorFactory.fromBitmap(resized))
                );
                mMap.setOnMarkerClickListener(this);
            }

        }
    }

    private Drawable resize(Drawable image) {
        Bitmap b = ((BitmapDrawable) image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 50, 50, false);
        return new BitmapDrawable(getResources(), bitmapResized);
    }
    @Override
    public boolean onMarkerClick(final Marker marker) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Review this place")
                .setMessage("Did you find a dustbin here?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        actionHandler.review(marker.getPosition().latitude, marker.getPosition().longitude, 1);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        actionHandler.review(marker.getPosition().latitude, marker.getPosition().longitude, -1);
                        dialogInterface.dismiss();
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        return false;
    }

}