import sys
from apiclient import sample_tools
from oauth2client import client
import xml.etree.ElementTree as ET
import os
import traceback


def main(argv):
	# Authenticate and construct service.
	service, flags = sample_tools.init(
			argv,
			'androidpublisher',
			'v3',
			__doc__,
			__file__,
			scope='https://www.googleapis.com/auth/androidpublisher')

	# Process flags and read their values.
	package_name = 'akhil.alltrans'
	import os

	d = './translations'
	folders = [os.path.join(d, o) for o in os.listdir(d)
										if os.path.isdir(os.path.join(d,o))]
	print(folders)

	allowed_languages = ['af', 'am', 'ar', 'az-AZ', 'be', 'bg', 'bn-BD', 'ca', 'cs-CZ', 'da-DK', 'de-DE', 'el-GR', 'en-AU', 'en-CA', 'en-GB', 'en-IN', 'en-SG', 'en-ZA', 'es-419', 'es-ES', 'es-US', 'et', 'eu-ES', 'fa', 'fa-AE', 'fa-AF', 'fa-IR', 'fi-FI', 'fr-CA', 'fr-FR', 'gl-ES', 'gu', 'hi-IN', 'hr', 'hu-HU', 'hy-AM', 'id', 'is-IS', 'it-IT', 'iw-IL', 'ja-JP', 'ka-GE', 'kk', 'km-KH', 'kn-IN', 'ko-KR', 'ky-KG', 'lo-LA', 'lt', 'lv', 'mk-MK', 'ml-IN', 'mn-MN', 'mr-IN', 'ms', 'ms-MY', 'my-MM', 'ne-NP', 'nl-NL', 'no-NO', 'pa', 'pl-PL', 'pt-BR', 'pt-PT', 'rm', 'ro', 'ru-RU', 'si-LK', 'sk', 'sl', 'sq', 'sr', 'sv-SE', 'sw', 'ta-IN', 'te-IN', 'th', 'tr-TR', 'uk', 'ur', 'vi', 'zh-CN', 'zh-HK', 'zh-TW', 'zu']

	to_be_skipped = True


	edit_request = service.edits().insert(body={}, packageName=package_name)
	result = edit_request.execute()
	edit_id = result['id']

	for lang in allowed_languages:
		for folder in folders:
			language = folder.split('values-', 1)[1]
			if lang.startswith(language):
				print(lang, language)
				try:
					root = ET.parse(os.path.join(folder, 'strings.xml')).getroot()
					long_desc = ''
					short_desc = ''
					app_name = ''
					for child in root:
						cur_text = child.text
						if cur_text[0] == '\"':
							cur_text = cur_text[1:]
						if cur_text[-1] == '\"':
							cur_text = cur_text[:-2]
						if child.tag =='string' and child.attrib['name'] == 'app_name':
							app_name = cur_text
						if child.tag =='string' and child.attrib['name'] == 'short_desc':
							short_desc = cur_text[:80]
						if child.tag =='string' and child.attrib['name'] == 'long_desc':
							long_desc = cur_text
					# print(app_name, short_desc, long_desc)

					listing_response_us = service.edits().listings().update(
							editId=edit_id, packageName=package_name, language=lang,
							body={'fullDescription': long_desc,
										'shortDescription': short_desc,
										'title': app_name}).execute()

					print ('Listing for language %s was updated.'
								% listing_response_us['language'])

					# print('Edit '%s' has been committed' % (commit_request['id']))
					# print('Edit '%s' has been committed' % edit_id)

				except client.AccessTokenRefreshError:
					print ('The credentials have been revoked or expired, please re-run the '
								'application to re-authorize')
				except Exception as e:
					print(traceback.format_exc())

	commit_request = service.edits().commit(
			editId=edit_id, packageName=package_name).execute()

if __name__ == '__main__':
	main(sys.argv)
