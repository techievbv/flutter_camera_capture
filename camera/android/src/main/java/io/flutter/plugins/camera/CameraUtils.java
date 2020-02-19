package io.flutter.plugins.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.OrientationEventListener;

import io.flutter.plugins.camera.Camera.ResolutionPreset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Provides various utilities for camera. */
public final class CameraUtils {

  private CameraUtils() { }

  private static Size previewSize;
  static int screenWidth = 0;
  static int screenHeight = 0;
  private static boolean isFrontFacing;


  static Size computeBestPreviewSize(String cameraName, CameraManager cameraManager, Activity activity) {
    try {
      CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraName);
      StreamConfigurationMap streamConfigurationMap =
              characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
      List<Size> sizes = Arrays.asList(streamConfigurationMap.getOutputSizes(SurfaceTexture.class));

      // Preview size and video size should not be greater than screen resolution or 1080.
      Point screenResolution = new Point();

      if (activity == null) {
        throw new IllegalStateException("No activity available!");
      }

      Display display = activity.getWindowManager().getDefaultDisplay();
      display.getSize(screenResolution);

      isFrontFacing =
              characteristics.get(CameraCharacteristics.LENS_FACING)
                      == CameraMetadata.LENS_FACING_FRONT;

      final boolean swapWH = getMediaOrientation() % 180 == 90;
      screenWidth = swapWH ? screenResolution.y : screenResolution.x;
      screenHeight = swapWH ? screenResolution.x : screenResolution.y;

      List<Size> goodEnough;

      goodEnough = getDesiredAspectRatiosList(sizes);

      if (goodEnough.isEmpty()) {
        previewSize = sizes.get(0);
      } else {
        previewSize = goodEnough.get(0);
        Collections.reverse(goodEnough);
      }

      Log.d("final preview size:", previewSize.toString());
    } catch (Exception ex) {
      Log.d("unhandled", "camera char exception");
    }

    return new Size(previewSize.getWidth(), previewSize.getHeight());
  }

  public static List<Map<String, Object>> getAvailableCameras(Activity activity)
      throws CameraAccessException {
    CameraManager cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    String[] cameraNames = cameraManager.getCameraIdList();
    List<Map<String, Object>> cameras = new ArrayList<>();
    for (String cameraName : cameraNames) {
      HashMap<String, Object> details = new HashMap<>();
      CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraName);
      details.put("name", cameraName);
      int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
      details.put("sensorOrientation", sensorOrientation);

      int lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
      switch (lensFacing) {
        case CameraMetadata.LENS_FACING_FRONT:
          details.put("lensFacing", "front");
          break;
        case CameraMetadata.LENS_FACING_BACK:
          details.put("lensFacing", "back");
          break;
        case CameraMetadata.LENS_FACING_EXTERNAL:
          details.put("lensFacing", "external");
          break;
      }
      cameras.add(details);
    }
    return cameras;
  }

  private static List<Size> getDesiredAspectRatiosList(List<Size> sizes) {
    List<Size> potentialSizes = new ArrayList<>();
    for (Size size : sizes) {
      if (size.getWidth() * 9 / 16 == size.getHeight() &&
              size.getWidth() <= screenWidth
              && size.getHeight() <= screenHeight
      ) {
        potentialSizes.add(size);
        Log.d("potential preview size", size.toString());
      }
    }
    return potentialSizes;
  }

  private static int getMediaOrientation() {
//    final int sensorOrientationOffset =
//            (currentOrientation == ORIENTATION_UNKNOWN)
//                    ? 0
//                    : (isFrontFacing) ? - currentOrientation : currentOrientation;
    return 90;
  }

  private static class CompareSizesByArea implements Comparator<Size> {
    @Override
    public int compare(Size lhs, Size rhs) {
      // We cast here to ensure the multiplications won't overflow.
      return Long.signum(
          (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
    }
  }
}
