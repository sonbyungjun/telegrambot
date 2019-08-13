<?php
/*
https://www.clien.net/service/board/cm_nas/11579612

<배경>
고기상자님 코드를 바탕으로 자막 파일만 받는 것만 했었다.
영화만 이 파일로 받고, 영화 이외에는 다른 분들이 업데이트해주시는 것으로 사용했으나
사이트가 자꾸 변경되어 여러 개를 같이 고쳐야 하기에 이 파일로 티프리카 모두 적용하는 것으로 변경한다.



<사용법>
1. 일반적인 사용법 
 1) 티프리카 파라미터 
	티프리카를 브라우저로 열어서 주소창에 있는 b_id와 sc 또는 ca 값을 그대로 사용한다. mode=list, x, y 생략
	- b_id : 게시판 이름
	- sc : 검색어
	- ca : 카테고리
 2) 전용 파라미터
	- sj_page	값 : 숫자	생략시 : 1
		탐색할 최대 페이지 값. 1이면 첫 페이지만, 이외는 지정된 페이지까지.

	- sj_download_mode  값: magnet / torrent		생략시: torrent
		filetender를 거치지 않고 마그넷을 반환

사용예) sj_page를 넣지 않을 경우 쿼리가 동일하다
드라마 720-next 검색시 
브라우저 주소창 : http://www.tfreeca22.com/board.php?b_id=tdrama&mode=list&sc=720p-next&x=40&y=22
=> http://자신의서버주소/sj_tf.php?b_id=tdrama&sc=720p-next

드라마 미드 탐색시 주소창
브라우저 주소창 : http://www.tfreeca22.com/board.php?mode=list&b_id=tdrama&ca=미드
=> http://자신의서버주소/sj_tf.php?b_id=tdrama&&ca=미드

다운로드 모드시 마그넷으로 받기
드라마 720-next
=> http://자신의서버주소/sj_tf.php?b_id=tdrama&sc=720p-next&sj_download_mode=magnet



2. 모든파일 다운로드(자막)
 1) sj_all	값 : off / on / dummy	생략시 off
	off이면 하나의 파일만, on이면 게시물에 있는 모든 파일을 받는다.
	영화나 애니에서 자막파일까지 받을 때 사용한다.
	on이면 파일 개수만큼 링크를 만들어야 하기에 오래 걸린다.

 2) sj_all=on 일때
	- sj_all_movie_only_1080p : on / off	생략시 off
		내가 필요해서 만들었다. 영화 탐색 시 1080p, 720p 토렌트 파일이 모두 존재할 때 720p 파일을 목록에서 제외한다.

	- sj_all_max : 생략시 20, 전체 받을경우 -1
		티프리카는 한페이지에 35개의 목록이 있다. 자막을 받기 위해 리스트를 생성하려면, 
		매 게시물마다 request를 보내서 토렌트와 자막을 리스트화한다.
		브라우저에 실행하면 2~3분 정도 걸리며 모두 갱신되나, synology download station 에서는 timeout이 발생한다.
		즉 한 페이지 모두를 생성하는데 걸리는 시간을 기다리지 못하여 에러가 발생한다. 이를 막고자 이 값을 주면 주어진 값 게시물만 리스트화한다.
		예) sj_all_max=20 일 경우 최근 게시물중 20개만 리턴
		갱신이 안 되는 것 같을 때 이 값을 변경.

	- sj_except_no_sub : on / off  생략시 off
		on일경우 무자막으로 나와있는 게시물은 받지 않는다
	
	- sj_sub_to_tar  값: on / off	생략시:off
		on일 경우 torrent, zip 파일이 아닌 파일은 tar로 압축해서 전송한다. (대부분 자막)

 3) sj_all=dummy 일때
	sj_all=on인 경우 갱신 시 타임아웃 걸릴 가능성도 있고, 한꺼번에 게시물이 올라올 경우 놓칠 수도 있다. 
	이를 피하기 위해 목록을 넘길 때는 dummy로 한 게시물당 4개씩 고정적으로 넘긴다. 다운로드 시에는 각 인덱스별로 파일을 받게된다.
	이 방법으로 할 경우에는 sj_all_movie_only_1080p 이 사용할 수가 없고, 한국영화 같이 한 게시물당 파일이 하나만 있을 경우 3개는 필요가 없어서
	0byte 짜리 sj_tf.php 파일이 계속 쌓이게 된다.
 
 4) sj_all=on_magnet 
	각 페이지 첫번째 마그넷을 리스트에 포함하여 반환한다. sj_download_mode=magnet은 첫번째 RSS 리스트에 이 페이지 주소가 들어간 후,
	실제 다운로드 받을려고 다시 연결할 때 마그넷을 넘긴다.
	sj_all=on_magnet 은 RSS 목록 요청시에 미리 마그넷 정보를 포함하는 방식이다
 
각 방식의 차이가 있으니 선택적으로 사용하기 바라며, 이를 회피하기 위해서는 미리 주기적으로 xml 파일을 만들어 놓고 이 고정 파일을 등록해서 
사용하는 방법이 가장 좋겠으나, 스케줄러 세팅하는 것도 또 귀찮은 일이다.

사용예)
영화 액션 카테고리 자막까지 받기
브라우저 주소창 : http://www.tfreeca22.com/board.php?mode=list&b_id=tmovie&ca=액션
=> http://자신의서버주소/sj_tf.php?b_id=tdrama&ca=액션&sj_all=on

영화 자막까지
http://자신의서버주소/tfreeca/sj_tf.php?b_id=tmovie&sj_all=on&sj_all_movie_only_1080p=on
http://자신의서버주소/tfreeca/sj_tf.php?b_id=tmovie&sj_all=on&sj_all_movie_only_1080p=on&sj_all_max=20
http://자신의서버주소/tfreeca/sj_tf.php?b_id=tmovie&sj_all=dummy



3. DLM
 다운로드 : https://github.com/soju6jan/soju6jan.github.io/tree/master/etc

 미리 만들어 놓은 2개의 DLM 있다.
 sj_tf_tv.dlm은 국내방송을, sj_tf_movie.dlm은 sj_all=on 모드로 검색하여 자막까지 표시되며, 
 sj_tf 파일이 web/tfreeca/ 폴더 아래에 있다는 가정하에 작성되어 있다.
 php 경로변경이 필요하거나 다른 게시판을 이용하려면 직접 수정한 후 dlm을 생성하면 된다.

 1) 압축풀기
 tar zxf sj_tf_tv.dlm
 하면 INFO search.php 파일이 나온다
 
 2) 압축하기
 tar zcf sj_tf_my.dlm INFO search.php

 3) 수정방법
  - info 파일에서 sj_tf_tv => sj_tf_my
  - search.php 파일에서 class 이름과 원하는 쿼리 변경
*/

class Tfreeca {
	public $filetender = 'http://www.filetender.com';
	public $listurl = 'http://www.tfreeca22.com/board.php?mode=list&';
	public $viewurl = 'http://www.tfreeca22.com/board.php?mode=view&';
	public $magneturl = 'http://www.tfreeca22.com/torrent_info.php?';
	public $param = '&sj_site=tf';

	public function parse($data) {
		$data = str_replace("</span>","",str_replace("<span class='sc_font'>","",str_replace("stitle1","stitle",str_replace("stitle2","stitle",str_replace("stitle3","stitle",str_replace("stitle4","stitle",str_replace("stitle5","stitle",str_replace("<tr class=\"bgcolor\">","<tr >",$data))))))));
		$data = explode("<tr >", $data);
		$ret = array();
		for($i = 1; $i < count($data); $i++){
			$info = array();
			$info[] = explode("\"",explode("<a href=\"board.php?mode=view&",$data[$i])[1])[0];
			if ( strpos($data[$i], 'stitle') ) $title = explode(" <",explode("class=\"stitle\"> ",$data[$i])[1])[0];
			else $title = explode(" <",explode("class=\"\"> ",$data[$i])[1])[0]; 
			$info[] = $title;
			$info[] = $data[$i];
			$ret[] = $info;
		}
		return $ret;
	}
}

class Torrentmi {
	public $filetender = 'https://www.filetender.com';
	public $listurl = 'https://www.torrentmi.com/list.php?';
	public $viewurl = 'https://www.torrentmi.com/view.php?';
	public $magneturl = 'http://img.torrentmi.com/info.php?';
	public $param = '&sj_site=tm';
	private $xpath_query = '//*[@id="contents"]/div[2]/div[3]/div[2]/table/tbody/tr';

	public function parse($data) {
		$doc = new DOMDocument();
		libxml_use_internal_errors(true);
		$doc->loadHTML($data);
		libxml_clear_errors();
		$xpath = new DOMXPath($doc);
		$entries = $xpath->query($this->xpath_query);
		$ret = array();
		foreach ($entries as $entry) {
			$info = array();
			$a_tag = $entry->firstChild->nextSibling->nextSibling->firstChild;
			$info[] = explode('?', $a_tag->getAttribute('href'))[1]; 
			$info[] = trim($a_tag->firstChild->nextSibling->nodeValue);
			$info[] = $entry->textContent;
			$ret[] = $info;
		}
		return $ret;
	}
}

$SITE = ( $_GET['sj_site'] == 'tm' ) ? new Torrentmi() : new Tfreeca();

$m = $_GET["sj_mode"];
if ( $m == 'd' ) {
	if ($_GET["sj_download_mode"] == 'magnet') redirect_magnet();
	else download();
} else {
	global $SITE;
	$query = '';
	foreach($_GET as $key => $value) if (startsWith($key, 'sj_') == false) $query = $query.'&'.$key.'='.$value;
	$ret = "<rss xmlns:showrss=\"http://showrss.info/\" version=\"2.0\"><channel><title>".$_GET["b_id"].' '.$_GET["sc"]."</title><description>sj_tf</description>";
	if ($_GET["b_id"] == null) {
		$board_list = array('tdrama', 'tent', 'tv');
		foreach($board_list as $b_id) {
			$url = $SITE->listurl.$query.'&b_id='.$b_id;
			$ret = make_rss($url, $ret);
		}
	} else {
		$url = $SITE->listurl.$query;
		$ret = make_rss($url, $ret);
	}
	$ret = $ret."</channel></rss>";
	header("Content-Type: application/xml");
	echo str_replace("&","&amp;",$ret);
}

function make_rss($url, $ret){
	global $SITE;
	$headers = array('Cookie: uuoobe=on;');
	$sj_page = $_GET["sj_page"];
	if ($sj_page == '') $sj_page = 1;
	$sj_all_max = $_GET["sj_all_max"];
	if ($sj_all_max == '') $sj_all_max = 20;
	$sj_download_mode = $_GET["sj_download_mode"];
	if ($sj_download_mode == '') $sj_download_mode = 'torrent';
	$sj_sub_to_tar = $_GET["sj_sub_to_tar"];
	if ($sj_sub_to_tar == '') $sj_sub_to_tar = 'off';

	for($page = 1 ; $page <= $sj_page ; $page++) {
		$url = $url.'&page='.$page;
		$data = get_html($url, $headers);
		$list = $SITE->parse($data);
		$count = 0;
		foreach ($list as $item) {
			$view = $item[0];
			$title = $item[1];
			if ( $_GET["sj_except_no_sub"] == 'on' && strpos($item[2], '무자막')) continue;
			if ( $_GET["sj_except_no_sub"] == 'on' && strpos($item[2], '영어')) continue;
			if ( $_GET["sj_all"] == 'on') {
				$url = $SITE->viewurl.$view;
				$data2 = get_html($url, $headers);
				$attachs = explode($SITE->filetender, $data2);
				if ( $_GET['sj_all_movie_only_1080p'] == 'on') {
					// TODO 이쁘게
					$flag_1080p = false;
					$index_720p = -1;
					for($x = 1 ; $x < count($attachs) ; $x++) {
						$filename = substr($attachs[$x], strpos($attachs[$x], '>')+1, strpos($attachs[$x] , '<')-strpos($attachs[$x], '>')-1);
						if ( endsWith($filename, '.torrent') ) {
							if (strpos($filename, '720p') > 0) $index_720p = $x;
							if (strpos($filename, '1080p') > 0) $flag_1080p = true;
						}
					}
					if ($flag_1080p && $index_720p != -1) $attachs[$index_720p] = '';
				}
				for($x = 1 ; $x < count($attachs) ; $x++) {
					$filename = substr($attachs[$x], strpos($attachs[$x], '>')+1, strpos($attachs[$x] , '<')-strpos($attachs[$x], '>')-1);
					$l = explode("\"",$attachs[$x])[0];
					if ( $filename != '' ) {
						$ret = $ret."<item><title>".$filename."</title><link>http://".$_SERVER['HTTP_HOST'].$_SERVER['PHP_SELF']."?" . $view . "&sj_mode=d&sj_download_mode=".$sj_download_mode."&sj_filename=".$filename."&sj_sub_to_tar=".$sj_sub_to_tar.$SITE->param."&sj_filetender=".$l."</link></item>";
					}
				}
				$count++;
				if ($sj_all_max != -1 && $count > $sj_all_max) break;
			} else if ( $_GET["sj_all"] == 'dummy') {
				for($idx = 0 ; $idx < 4 ; $idx++) {
					$ret = $ret."<item><title>".$title."</title><link>http://".$_SERVER['HTTP_HOST'].$_SERVER['PHP_SELF']."?" . $view . "&sj_mode=d&sj_download_mode=".$sj_download_mode."&sj_idx=".$idx."&sj_sub_to_tar=".$sj_sub_to_tar.$SITE->param."</link></item>";
				}
			} else if ( $_GET["sj_all"] == 'on_magnet') {
				$count = preg_match('/(^|\?|\&)b_id\=(?P<bo_table>.*?)($|&)/', $view, $match);
				if ( $count == 1) $bo_table = $match[bo_table];
				$count = preg_match('/(^|\?|\&)id\=(?P<wr_id>.*?)($|&)/', $view, $match);
				if ( $count == 1) $wr_id = $match[wr_id];
				$ret = $ret."<item><title>".$title."</title><link>".get_magnet($bo_table, $wr_id)."</link></item>";
			}
			else {
				$ret = $ret."<item><title>".$title."</title><link>http://".$_SERVER['HTTP_HOST'].$_SERVER['PHP_SELF']."?" . $view . "&sj_mode=d&sj_download_mode=".$sj_download_mode.$SITE->param."</link></item>";
			}
		}
		if ($sj_all_max != -1 && $count > $sj_all_max) break;
	}
	return $ret;
}

function download() {
	global $SITE;
	$b_id = $_GET["b_id"];
	$id = $_GET["id"];
	if ( $_GET["sj_filename"] == '') {
		$ret = get_torrent();
		if ($ret == null) return;
		$sj_filename = $ret[0];
		$sj_filetender = $ret[1];
	} else {
		$sj_filename = $_GET["sj_filename"];
		$sj_filetender= $_GET["sj_filetender"];
	}
	$url = $SITE->viewurl.'b_id=' . $b_id . '&id=' . $id;
	$url2 = $SITE->filetender.$sj_filetender;
	$headers = array(
		'Referer: '.$url,
		'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8',
		'Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7',
		'Connection: keep-alive',
		'Host: www.filetender.com',
		'Upgrade-Insecure-Requests: 1',
		'User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36'
	);
	$data = get_html($url2,  $headers);
	$url3 = explode("'", explode("var newUrl = '", $data)[1])[0];
	$headers[0] = 'Referer: '.$url2;
	$headers[4] = 'Referer: '.'file.filetender.com';
	// 2018-11-20
	$count = preg_match_all('/input\s+type\=\".*?\"\s+name\=\"(?P<name>.*?)\"\s+value\=\"(?P<value>.*?)\"/', $data, $match);
	$query = '';
	for($i = 0; $i < $count; $i++){
		if ( $i != 0 ) $query = $query.'&';
		$query = $query.$match[name][$i].'='.$match[value][$i];
	}

	// 2018-11-18 post -> get.
	$method = explode('"', explode('form method="', $data)[1])[0];
	if (!strcmp($method, 'get')) {
		$url3 = $url3.'?'.$query;
		$query = null;
	}
	$data = get_html($url3,  $headers, $query);
	header("Content-Type: application/octet-stream");
	if ($_GET["sj_sub_to_tar"] == 'on' && endsWith($sj_filename, ".torrent")==false && endsWith($sj_filename, ".zip")==false ) {
		header("content-disposition: attachment; filename=\"".$sj_filename.".tar\"");
		echo tarSection($sj_filename, $data);
	} else {
		header("Content-Disposition: attachment; filename=\"".$sj_filename."\"");
		echo $data;
	}
}

function redirect_magnet() {
	$ret = get_magnet();
	header('Location: '.$ret);
}

function get_magnet($bo_table=null, $wr_id=null) {
	global $SITE;
	if ($bo_table == null) {
		$bo_table = $_GET["b_id"];
		$wr_id = $_GET["id"];
	}
	$url = $SITE->magneturl.'bo_table=' . $bo_table . '&wr_id=' . $wr_id;
	$data = get_html($url,  array());
	$tmp = explode('a href="magnet', $data);
	$tmp = explode('"', $tmp[1]);
	$ret = 'magnet'.$tmp[0];
	return $ret;
}

function get_torrent() {
	global $SITE;
	$b_id = $_GET["b_id"];
	$id = $_GET["id"];
	$sj_idx = $_GET["sj_idx"];
	if ($sj_idx == '') $sj_idx = 0;
	$url = $SITE->viewurl.'b_id='.$b_id.'&id='.$id;
	$headers[] = 'Cookie: uuoobe=on;';
	$data2 = get_html($url, $headers);
	$attachs = explode($SITE->filetender, $data2);
	$idx = -1;
	for($x = 1 ; $x < count($attachs) ; $x++) {
		$filename = substr($attachs[$x], strpos($attachs[$x], '>')+1, strpos($attachs[$x] , '<')-strpos($attachs[$x], '>')-1);
		$l = explode("\"",$attachs[$x])[0];
		if ( $filename != '' ) {
			$idx++;
			if ($idx == $sj_idx) return array($filename, $l);
		}
	}
	return null;
}

function get_html() {
	$url = func_get_arg(0);
	$headers = func_get_arg(1);
	$query = (func_num_args() == 3) ? func_get_arg(2) : null;
	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL, $url);
	curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
	curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);
	curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	curl_setopt($ch, CURLOPT_HEADER, 0);
	curl_setopt($ch, CURLOPT_COOKIEJAR, 'cookies.txt');
	curl_setopt($ch, CURLOPT_COOKIEFILE, 'cookies.txt');
	if ( $query != null) {
		curl_setopt($ch, CURLOPT_POST, 1);
		curl_setopt($ch, CURLOPT_POSTFIELDS, $query);
	}
	$data = curl_exec($ch);
	return $data;
}

function startsWith($haystack, $needle) {
     $length = strlen($needle);
     return (substr($haystack, 0, $length) === $needle);
}

function endsWith($haystack, $needle) {
    $length = strlen($needle);
    if ($length == 0) {
        return true;
    }
    return (substr($haystack, -$length) === $needle);
}

#https://stackoverflow.com/questions/16506859/compress-on-the-fly-a-directory-in-tar-gz-format-using-php
// Computes the unsigned Checksum of a file’s header
// to try to ensure valid file
// PRIVATE ACCESS FUNCTION
function __computeUnsignedChecksum($bytestring) {
  for($i=0; $i<512; $i++)
    $unsigned_chksum += ord($bytestring[$i]);
  for($i=0; $i<8; $i++)
    $unsigned_chksum -= ord($bytestring[148 + $i]);
  $unsigned_chksum += ord(" ") * 8;

  return $unsigned_chksum;
}

// Generates a TAR file from the processed data
// PRIVATE ACCESS FUNCTION
function tarSection($Name, $Data, $information=NULL) {
  // Generate the TAR header for this file

  $header .= str_pad($Name,100,chr(0));
  $header .= str_pad("777",7,"0",STR_PAD_LEFT) . chr(0);
  $header .= str_pad(decoct($information["user_id"]),7,"0",STR_PAD_LEFT) . chr(0);
  $header .= str_pad(decoct($information["group_id"]),7,"0",STR_PAD_LEFT) . chr(0);
  $header .= str_pad(decoct(strlen($Data)),11,"0",STR_PAD_LEFT) . chr(0);
  $header .= str_pad(decoct(time(0)),11,"0",STR_PAD_LEFT) . chr(0);
  $header .= str_repeat(" ",8);
  $header .= "0";
  $header .= str_repeat(chr(0),100);
  $header .= str_pad("ustar",6,chr(32));
  $header .= chr(32) . chr(0);
  $header .= str_pad($information["user_name"],32,chr(0));
  $header .= str_pad($information["group_name"],32,chr(0));
  $header .= str_repeat(chr(0),8);
  $header .= str_repeat(chr(0),8);
  $header .= str_repeat(chr(0),155);
  $header .= str_repeat(chr(0),12);

  // Compute header checksum
  $checksum = str_pad(decoct(__computeUnsignedChecksum($header)),6,"0",STR_PAD_LEFT);
  for($i=0; $i<6; $i++) {
    $header[(148 + $i)] = substr($checksum,$i,1);
  }
  $header[154] = chr(0);
  $header[155] = chr(32);

  // Pad file contents to byte count divisible by 512
  $file_contents = str_pad($Data,(ceil(strlen($Data) / 512) * 512),chr(0));

  // Add new tar formatted data to tar file contents
  $tar_file = $header . $file_contents;

  return $tar_file;
}
?>
