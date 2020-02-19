
Flutter camera plugin updated with fix for Camera preview and captured images stretch issue.

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


Pull requests are welcome !!
