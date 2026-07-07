DOMPurify.addHook('uponSanitizeAttribute', function(node, event) {
	if(node.nodeName.toLowerCase() !== 'em' || event.attrName.toLowerCase() !== 'class')
	{
		node.removeAttribute(event.attrName);
	}
});

ACC.sanitizer = {
	config: {
				ALLOWED_TAGS: ['pre', 'address', 'em', 'hr', "p", "div", "h1", "h2", "h3", "h4", "h5", "h6", "ul", "ol", "li",
                               "blockquote", 'b', 'i', 's', 'o', 'sup', 'sub', 'ins', 'del', 'strong', 'strike', 'tt', 'code', 'big', 'small', 'br', 'span']
	},
		  
	configSelect: {
				ALLOWED_TAGS: ['pre', 'address', 'hr', "p", "div", "h1", "h2", "h3", "h4", "h5", "h6", "ul", "ol", "li",
                               "blockquote", 'b', 'i', 's', 'o', 'sup', 'sub', 'ins', 'del', 'strong', 'strike', 'tt', 'code', 'big', 'small', 'br', 'span']
	},

	sanitize: function(dirty) {
		return DOMPurify.sanitize(dirty, ACC.sanitizer.config);
	},

	sanitizeSelect: function(dirty) {
		return DOMPurify.sanitize(dirty, ACC.sanitizer.configSelect);
	},

	sanitizeJson: function(dirty)  {
        let sanitizedObject = {};

        Object.keys(dirty).forEach(function(key) {
            let value = dirty[key];

            if (typeof value === 'string') {
                sanitizedObject[key] = ACC.sanitizer.sanitize(value);
            } else if (Array.isArray(value)) {
                sanitizedObject[key] = ACC.sanitizer.sanitizeArray(value);
            } else if (typeof value === 'object' && value !== null) {
                sanitizedObject[key] = ACC.sanitizer.sanitizeJson(value);
            } else {
                sanitizedObject[key] = value;
            }
        });

        return sanitizedObject;
	},

    sanitizeArray: function(array) {
        return array.map(item => {
            if (typeof item === 'string') {
                return ACC.sanitizer.sanitize(item);
            } else if (Array.isArray(item)) {
                return ACC.sanitizer.sanitizeArray(item);
            } else if (typeof item === 'object' && item !== null) {
                return ACC.sanitizer.sanitizeJson(item);
            } else {
                return item;
            }
        });
    }
};
