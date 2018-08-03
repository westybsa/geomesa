/***********************************************************************
 * Copyright (c) 2013-2018 Commonwealth Computer Research, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 ***********************************************************************/

package org.locationtech.geomesa.fs.storage.common

import com.vividsolutions.jts.geom.{Envelope, Geometry}
import org.geotools.geometry.jts.ReferencedEnvelope
import org.locationtech.geomesa.fs.storage.api.FileSystemWriter
import org.opengis.feature.simple.SimpleFeature

trait MetadataObservingFileSystemWriter extends FileSystemWriter {
  def metadata: org.locationtech.geomesa.fs.storage.api.FileMetadata

  private var count = 0
  private var bounds: Envelope = _

  override def write(feature: SimpleFeature): Unit = {
    // Update internal count/bounds/etc
    count += 1
    if (bounds == null) {
      bounds = feature.getDefaultGeometry.asInstanceOf[Geometry].getEnvelopeInternal
    } else {
      bounds = {
        bounds.expandToInclude(feature.getDefaultGeometry.asInstanceOf[Geometry].getEnvelopeInternal)
        bounds
      }
    }
    writeInternal(feature)
  }



  def writeInternal(feature: SimpleFeature): Unit

  def closeInternal(): Unit

  override def close(): Unit = {
    // Finalize metadata
    metadata.increaseFeatureCount(count)
    metadata.expandBounds(bounds)
    closeInternal()
  }
}
