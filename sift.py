import numpy as np
import cv2 as cv
import matplotlib.pyplot as plt

img1 = cv.imread('forged.png', cv.IMREAD_GRAYSCALE)
img2 = cv.imread('real.png', cv.IMREAD_GRAYSCALE)

sift = cv.SIFT_create()

kp1, desc1 = sift.detectAndCompute(img1, None)
kp2, desc2 = sift.detectAndCompute(img2, None)

# img = cv.drawKeypoints(img, keypoints, None)
# img = cv.drawKeypoints(img,keypoints,img,flags=cv.DRAW_MATCHES_FLAGS_DRAW_RICH_KEYPOINTS)

# cv.imshow("Image", img)
# cv.waitKey(0)
# cv.destroyAllWindows()

bf = cv.BFMatcher()
matches = bf.knnMatch(desc1, desc2, k=2)

print(len(matches))
print(len(matches[0]))

good = []
for m,n in matches:
    if m.distance < 0.75 * n.distance:
        good.append([m])

img3 = cv.drawMatchesKnn(img1, kp1, img2, kp2, good, None, flags=cv.DrawMatchesFlags_NOT_DRAW_SINGLE_POINTS)


plt.imshow(img3),plt.show()