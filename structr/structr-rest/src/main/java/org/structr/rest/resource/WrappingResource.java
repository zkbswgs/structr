/*
 *  Copyright (C) 2011 Axel Morgner
 *
 *  This file is part of structr <http://structr.org>.
 *
 *  structr is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  structr is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */



package org.structr.rest.resource;

import org.structr.common.error.FrameworkException;
import org.structr.rest.RestMethodResult;
import org.structr.rest.exception.IllegalPathException;

//~--- JDK imports ------------------------------------------------------------

import java.util.Map;

//~--- classes ----------------------------------------------------------------

/**
 * A resource constraint that implements the generic ability to
 * wrap another constraint.
 *
 * @author Christian Morgner
 */
public abstract class WrappingResource extends Resource {

	protected Resource wrappedResource = null;

	//~--- methods --------------------------------------------------------

	@Override
	public RestMethodResult doPost(Map<String, Object> propertySet) throws FrameworkException {

		if (wrappedResource != null) {

			return wrappedResource.doPost(propertySet);

		}

		throw new IllegalPathException();
	}

	@Override
	public RestMethodResult doHead() throws FrameworkException {

		if (wrappedResource != null) {

			return wrappedResource.doHead();

		}

		throw new IllegalPathException();
	}

	@Override
	public RestMethodResult doOptions() throws FrameworkException {

		if (wrappedResource != null) {

			return wrappedResource.doOptions();

		}

		throw new IllegalPathException();
	}

	protected void wrapResource(Resource wrappedResource) {

		this.idProperty      = wrappedResource.idProperty;
		this.wrappedResource = wrappedResource;
	}

	@Override
	public Resource tryCombineWith(Resource next) throws FrameworkException {

		if (next instanceof WrappingResource) {

			((WrappingResource) next).wrapResource(this);

			return next;

		}

		return null;
	}

	//~--- get methods ----------------------------------------------------

	@Override
	public String getUriPart() {
		return wrappedResource.getUriPart();
	}

	@Override
	public boolean isCollectionResource() throws FrameworkException {

		if (wrappedResource != null) {

			return wrappedResource.isCollectionResource();

		}

		throw new IllegalPathException();
	}
}