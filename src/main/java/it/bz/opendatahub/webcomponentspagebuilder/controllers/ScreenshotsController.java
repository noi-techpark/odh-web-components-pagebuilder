package it.bz.opendatahub.webcomponentspagebuilder.controllers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import it.bz.opendatahub.webcomponentspagebuilder.ApplicationDeployment;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;
import it.bz.opendatahub.webcomponentspagebuilder.events.PageVersionRemovedEvent;
import it.bz.opendatahub.webcomponentspagebuilder.events.PageVersionUpdatedEvent;
import it.bz.opendatahub.webcomponentspagebuilder.rendering.screenshots.ScreenshotRenderer;

/**
 * Controller that is responsible for the actual generation of a page version
 * screenshot, which also manages a in-memory cache that will be cleaned up
 * (based on recent access/use) at periodic intervals.
 * 
 * @author danielrampanelli
 */
@Component
public class ScreenshotsController {

	private class CachedPageVersionScreenshot {

		private byte[] image;

		private LocalDateTime accessDatetime;

		public CachedPageVersionScreenshot() {

		}

		public byte[] getImage() {
			return image;
		}

		public void setImage(byte[] image) {
			this.image = image;
		}

		public LocalDateTime getAccessDatetime() {
			return accessDatetime;
		}

		public void setAccessDatetime(LocalDateTime accessDatetime) {
			this.accessDatetime = accessDatetime;
		}

	}

	public class ScreenshotRenderingInProgressException extends Exception {

		private static final long serialVersionUID = 1109863583393983313L;

	}

	@Autowired
	ApplicationDeployment currentDeployment;

	@Autowired(required = false)
	ScreenshotRenderer renderer;

	private Set<String> queue = Collections.synchronizedSet(new HashSet<String>());

	private Map<String, CachedPageVersionScreenshot> cache = Collections.synchronizedMap(new HashMap<>());

	private void generateScreenshot(String versionID) {
		String url = String.format("%s/pages/preview/%s", currentDeployment.getBaseUrl(), versionID);

		queue.add(versionID);

		byte[] image = renderer.renderScreenshot(url);

		queue.remove(versionID);

		CachedPageVersionScreenshot cachedScreenshot = new CachedPageVersionScreenshot();
		cachedScreenshot.setImage(image);
		cachedScreenshot.setAccessDatetime(LocalDateTime.now());

		cache.put(versionID, cachedScreenshot);
	}

	public byte[] get(PageVersion pageVersion) throws ScreenshotRenderingInProgressException {
		if (renderer == null) {
			return null;
		}

		String key = pageVersion.getIdAsString();

		if (queue.contains(key)) {
			throw new ScreenshotRenderingInProgressException();
		}

		try {
			if (!cache.containsKey(key)) {
				generateScreenshot(pageVersion.getIdAsString());
			}

			cache.get(key).setAccessDatetime(LocalDateTime.now());

			return cache.get(key).getImage();
		} catch (IllegalStateException e) {
			return null;
		}
	}

	@EventListener
	public void on(PageVersionUpdatedEvent event) {
		cache.remove(event.getVersionId());

		new Thread(() -> {
			generateScreenshot(event.getVersionId());
		}).start();
	}

	@EventListener
	public void on(PageVersionRemovedEvent event) {
		cache.remove(event.getVersionId());
	}

	@Scheduled(cron = "0 0 0/8 * * ?")
	public void cleanup() {
		LocalDateTime now = LocalDateTime.now();

		for (String key : cache.keySet()) {
			if (cache.get(key).getAccessDatetime().until(now, ChronoUnit.HOURS) >= 24) {
				cache.remove(key);
			}
		}
	}

}
