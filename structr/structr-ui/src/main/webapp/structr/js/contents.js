/*
 *  Copyright (C) 2012 Axel Morgner
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

var contents, editor;

$(document).ready(function() {
    Structr.registerModule('contents', _Contents);
    Structr.classes.push('content');
});

var _Contents = {

    icon : 'icon/page_white.png',
    add_icon : 'icon/page_white_add.png',
    delete_icon : 'icon/page_white_delete.png',
	
    init : function() {
    //Structr.classes.push('content');
    },

    onload : function() {
	if (debug) console.log('onload');
	if (palette) palette.remove();
	main.append('<div id="contents"></div>');

	contents = $('#contents');
	_Contents.refresh();
	contents.show();
    },

    refresh : function() {
	contents.empty();
	if (_Contents.show()) {
	    contents.append('<button class="add_content_icon button"><img title="Add Content" alt="Add Content" src="' + _Contents.add_icon + '"> Add Content</button>');
	    $('.add_content_icon', main).on('click', function() {
		var entity = {};
		entity.type = 'Content';
		_Entities.create(this, entity);
	    });
	}
    },

    show : function() {
	if (palette) {
	    palette.append('<div class="elementGroup"><h3>Content</h3><div class="draggable content" id="add_content">content</div></div>');
	    $('#add_content', palette).draggable({
		iframeFix: true,
		revert: 'invalid',
		containment: 'body',
		zIndex: 1,
		helper: 'clone'
	    });
	}

	return _Entities.list('Content');
    },

    appendContentElement : function(content, parentId, resourceId) {
	if (debug) console.log('Contents.appendContentElement: parentId: ' + parentId + ', resourceId: ' + resourceId);

	var parent = Structr.findParent(parentId, resourceId, contents);

	var text = (content.content ? content.content.substring(0,200) : '&nbsp;');

	parent.append('<div class="node content ' + content.id + '_">'
	    + '<img class="typeIcon" src="'+ _Contents.icon + '">'
	    + '<b class="content_">' + text + '</b> <span class="id">' + content.id + '</span>'
	    + '</div>');
	var div = $('.' + content.id + '_', parent);

	div.append('<img title="Edit ' + content.name + ' [' + content.id + ']" alt="Edit ' + content.name + ' [' + content.id + ']" class="edit_icon button" src="icon/pencil.png">');
	$('.edit_icon', div).on('click', function() {
	    Structr.dialog('Edit content of ' + content.id, function() {
		console.log('content saved')
	    }, function() {
		console.log('cancelled')
	    });
	    _Contents.editContent(this, content, 'all', $('#dialogBox .dialogText'));
	});

	div.append('<img title="Delete content \'' + content.name + '\'" alt="Delete content \'' + content.name + '\'" class="delete_icon button" src="' + Structr.delete_icon + '">');
	$('.delete_icon', div).on('click', function() {
	    _Contents.deleteContent(this, content);
	});
	$('b', div).on('click', function() {
	    Structr.dialog('Edit Properties of ' + content.id, function() {
		console.log('save')
	    }, function() {
		console.log('cancelled')
	    });
	    _Entities.showProperties(this, content, $('#dialogBox .dialogText'));
	});

	return div;
    },

    deleteContent : function(button, content) {
	if (debug) console.log('delete content ' + content);
	deleteNode(button, content);
    },
	
    patch : function(id, text1, text2) {
	if (debug) console.log(text1, text2);

	// Avoid null values
	if (!text1) text1 = '';
	if (!text2) text2 = '';

	var p = dmp.patch_make(text1, text2);
	var strp = dmp.patch_toText(p);
	if (debug) console.log(strp, $.quoteString(strp));

	var obj = {};
	obj.command = 'PATCH';
	obj.id = id;
	var data = {};
	data.patch = strp;
	obj.data = data;

	if (debug) console.log(obj);
	return sendObj(obj);
    },

    editContent : function (button, entity, view, element) {
	if (isDisabled(button)) return;
	var div = element.append('<div class="editor"></div>');
	if (debug) console.log(div);
	var contentBox = $('.editor', element);
	editor = CodeMirror(contentBox.get(0), {
	    value: unescapeTags(entity.content),
	    mode:  "htmlmixed",
	    lineNumbers: true,
	    onChange: function(cm, changes) {
				
		var element = $( '.' + entity.id + '_')[0];
		var text1 = $(element).children('.content_').text();
		var text2 = editor.getValue();
				
		console.log(element);
		console.log(text1, text2);

		if (text1 == text2) return;
		editorCursor = cm.getCursor();
		console.log(editorCursor);
				
		_Contents.patch(entity.id, text1, text2);
				
	    }
	});

	editor.id = entity.id;

    }
};