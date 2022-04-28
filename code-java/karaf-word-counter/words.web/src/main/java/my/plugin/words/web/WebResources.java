package my.plugin.words.web;

import org.osgi.service.component.annotations.Component;

@Component(service = WebResources.class, property = {
        "osgi.http.whiteboard.resource.pattern=/word/*",
        "osgi.http.whiteboard.resource.prefix=/public"
})
public class WebResources{

}
