package co.rxstack.ml.aggregator;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import co.rxstack.ml.aggregator.model.PotentialFace;

public interface IFaceExtractorService {

	List<PotentialFace> detectFaces(BufferedImage image);

}
